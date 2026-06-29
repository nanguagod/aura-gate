package com.auragate.ai.config;

import com.auragate.ai.websocket.AiWebSocketHandler;
import com.auragate.rbac.configure.TokenService;
import com.auragate.rbac.domain.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * AI WebSocket 配置 — 注册 AiWebSocketHandler 到 /ws/ai
 * 通过 HandshakeInterceptor 在握手阶段解析 URL 中的 JWT token，
 * 提取 userId 存入 session attributes，供 AiWebSocketHandler 使用。
 */
@Configuration
@EnableWebSocket
public class WebSocketAiConfig implements WebSocketConfigurer {

    @Resource
    private AiWebSocketHandler aiWebSocketHandler;

    @Resource
    private TokenService tokenService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(aiWebSocketHandler, "/ws/ai")
                .setAllowedOrigins("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String query = request.getURI().getQuery();
                        if (query != null) {
                            for (String param : query.split("&")) {
                                String[] pair = param.split("=", 2);
                                if (pair.length == 2 && "token".equals(pair[0])) {
                                    try {
                                        Claims claims = tokenService.parseToken(pair[1]);
                                        String userJson = claims.get("user_key", String.class);
                                        LoginUser loginUser = objectMapper.readValue(userJson, LoginUser.class);
                                        attributes.put("userId", loginUser.getUserId());
                                    } catch (Exception ignored) {
                                        // token 无效 — 允许连接但 userId 为 0
                                    }
                                    break;
                                }
                            }
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                });
    }
}
