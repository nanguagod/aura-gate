package com.auragate.ai.config;

import com.auragate.ai.websocket.AiWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * AI WebSocket 配置 — 注册 AiWebSocketHandler 到 /ws/ai
 */
@Configuration
@EnableWebSocket
public class WebSocketAiConfig implements WebSocketConfigurer {

    @Resource
    private AiWebSocketHandler aiWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(aiWebSocketHandler, "/ws/ai")
                .setAllowedOrigins("*");
    }
}
