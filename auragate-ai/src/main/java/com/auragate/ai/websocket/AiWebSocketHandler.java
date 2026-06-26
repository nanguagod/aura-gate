package com.auragate.ai.websocket;

import com.auragate.ai.service.AiConversationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * AI WebSocket 处理器 — 接收用户消息，流式返回 AI 回复
 * 端点：/ws/ai（需在 WebSocketConfig 中注册）
 */
@Slf4j
@Component
public class AiWebSocketHandler extends TextWebSocketHandler {

    @Resource
    private AiConversationService conversationService;

    /** 在线会话列表 */
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket 连接建立: id={}, userId={}", session.getId(), getUserId(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = getUserId(session);
        String payload = message.getPayload();

        // 1. 保存用户消息
        conversationService.saveUserMessage(parseUserId(userId), payload);

        // 2. 获取上下文
        var context = conversationService.getContext(parseUserId(userId), 10);

        // 3. 发送流式回复（简化版 — 后续可对接 AuraAgent）
        StringBuilder sb = new StringBuilder();
        for (String ctx : context) {
            sb.append(ctx).append("\n");
        }
        String reply = "收到消息: " + payload + "\n上下文:\n" + sb;

        // 模拟逐 token 推送
        for (char c : reply.toCharArray()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(String.valueOf(c)));
                Thread.sleep(10); // 模拟延迟
            }
        }
        // 发送结束标记
        if (session.isOpen()) {
            session.sendMessage(new TextMessage("[DONE]"));
        }
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
