package com.auragate.ai.controller;

import com.auragate.ai.agent.AuraAgent;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 控制器
 * <p><b>注意：</b>{@link #doChatWithAgent(String)} SSE 流式端点已弃用，
 * 请使用 WebSocket 端点 {@code /ws/ai} 进行流式对话。
 * 参见 {@link com.auragate.ai.websocket.AiWebSocketHandler}</p>
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel openAiChatModel;

    /**
     * 流式调用 AuraAgent 超级智能体
     *
     * @deprecated 已废弃，推荐使用 WebSocket 端点 /ws/ai
     * （{@link com.auragate.ai.websocket.AiWebSocketHandler}）
     * 通过 WebSocket 实现流式对话，避免 SSE 的长连接开销。
     * <p>此端点保留仅用于向后兼容，后续版本可能移除。</p>
     */
    @Deprecated
    @GetMapping("/agent/chat")
    public SseEmitter doChatWithAgent(String message) {
        AuraAgent auraAgent = new AuraAgent(allTools, openAiChatModel);
        return auraAgent.runStream(message);
    }
}
