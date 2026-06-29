package com.auragate.ai.websocket;

import com.auragate.ai.agent.AuraAgent;
import com.auragate.ai.service.AiConversationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * AI WebSocket 处理器 — 接收用户消息，流式返回 AI 回复
 * 端点：/ws/ai（在 WebSocketAiConfig 中注册）
 *
 * 使用 AuraAgent 处理对话，通过 WebSocket 逐步骤推送结果，
 * 以 [DONE] 标记结束。
 */
@Slf4j
@Component
public class AiWebSocketHandler extends TextWebSocketHandler {

    @Resource
    private AiConversationService conversationService;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel openAiChatModel;

    /** 在线会话列表 */
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket 连接建立: id={}, userId={}", session.getId(), getUserId(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userId = getUserId(session);
        String payload = message.getPayload();
        Long uid = parseUserId(userId);

        // 1. 保存用户消息
        conversationService.saveUserMessage(uid, payload);

        // 2. 创建 AuraAgent 并流式运行
        AuraAgent agent = new AuraAgent(allTools, openAiChatModel);

        agent.runStream(payload,
                // dataEmitter — 每步结果推送到 WebSocket
                data -> {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(data));
                        } catch (Exception e) {
                            log.error("WebSocket 发送失败", e);
                        }
                    }
                },
                // onComplete
                () -> {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage("[DONE]"));
                        } catch (Exception e) {
                            log.error("WebSocket 发送 [DONE] 失败", e);
                        }
                    }
                },
                // onError
                e -> {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage("执行错误：" + e.getMessage()));
                            session.sendMessage(new TextMessage("[DONE]"));
                        } catch (Exception ex) {
                            log.error("WebSocket 发送错误信息失败", ex);
                        }
                    }
                }
        );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket 连接关闭: id={}", session.getId());
    }

    private String getUserId(WebSocketSession session) {
        Object uid = session.getAttributes().get("userId");
        return uid != null ? uid.toString() : "0";
    }

    private Long parseUserId(String userId) {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
