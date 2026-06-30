package com.auragate.ai.agent;

import cn.hutool.core.util.StrUtil;
import com.auragate.ai.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * <p>
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 1、基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 2、执行，更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            // 执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // 单步执行
                String stepResult = step();
                // 直接输出步骤结果，不加 Step 前缀（step 信息已记录在日志中）
                results.add(stepResult);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 3、清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理（流式输出，通过回调推送数据）
     * <p>推荐方式：可用于 WebSocket 等非 HTTP 流式场景</p>
     * <p><b>重要：</b>不会推送中间工具调用结果到前端，只在最终完成时推送最终答案。</p>
     *
     * @param userPrompt  用户提示词
     * @param dataEmitter 最终结果回调（只调用一次）
     * @param onComplete  完成回调
     * @param onError     错误回调
     */
    public void runStream(String userPrompt, Consumer<String> dataEmitter,
                          Runnable onComplete, Consumer<Exception> onError) {
        CompletableFuture.runAsync(() -> {
            try {
                // 1、基础校验
                if (this.state != AgentState.IDLE) {
                    dataEmitter.accept("错误：无法从状态运行代理：" + this.state);
                    onComplete.run();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    dataEmitter.accept("错误：不能使用空提示词运行代理");
                    onComplete.run();
                    return;
                }
                // 2、执行，更改状态
                this.state = AgentState.RUNNING;
                // 记录消息上下文
                messageList.add(new UserMessage(userPrompt));
                // 执行循环 — 不推送中间结果到前端
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                    step();
                }
                // 3、推送最终答案
                String answer = getFinalAnswer();
                if (StrUtil.isNotBlank(answer)) {
                    dataEmitter.accept(answer);
                } else if (currentStep >= maxSteps && state != AgentState.FINISHED) {
                    state = AgentState.FINISHED;
                    dataEmitter.accept("处理达到最大步骤（" + maxSteps + "），请尝试更具体的问题。");
                }
                onComplete.run();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent", e);
                onError.accept(e);
            } finally {
                // 4、清理资源
                this.cleanup();
            }
        });
    }

    /**
     * 运行代理（流式输出，SseEmitter）
     * <p>内部委托给 {@link #runStream(String, Consumer, Runnable, Consumer)}</p>
     *
     * @param userPrompt 用户提示词
     * @return SseEmitter
     * @deprecated 推荐使用 WebSocket + 回调式 {@link #runStream(String, Consumer, Runnable, Consumer)}
     */
    @Deprecated
    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 分钟超时

        runStream(userPrompt,
                data -> {
                    try { sseEmitter.send(data); } catch (IOException e) { throw new RuntimeException(e); }
                },
                () -> {
                    try { sseEmitter.complete(); } catch (Exception ignored) {}
                },
                e -> {
                    try { sseEmitter.send("执行错误：" + e.getMessage()); sseEmitter.complete(); }
                    catch (IOException ex) { sseEmitter.completeWithError(ex); }
                }
        );

        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }

    /**
     * 定义单个步骤
     *
     * @return
     */
    public abstract String step();

    /**
     * 获取最终答案（子类覆盖）
     */
    public String getFinalAnswer() {
        return "";
    }

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}
