package com.xf.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * WebSocket 认证拦截器 (方案 B: 更安全专业)
 * <p>
 * 不再从 URL 获取参数，而是直接在 STOMP 协议的 CONNECT 帧头中获取认证信息。
 * 这种方式更符合生产环境标准，可以防止 Token 在 URL 日志中泄露。
 */
@Component
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 只有在 CONNECT 阶段才进行认证
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从 STOMP 头部获取 "username" (生产环境中这里通常是 "Authorization": "Bearer token")
            // 前端 connect() 时需要传递这个 header
            List<String> authorization = accessor.getNativeHeader("username");

            log.info("WebSocket Interceptor: Connecting...");

            if (authorization != null && !authorization.isEmpty()) {
                String username = authorization.get(0);
                if (username != null && !username.isEmpty()) {
                    // 绑定 User 到 WebSocket Session
                    // 之后 Controller 里的 Principal 就有值了
                    accessor.setUser(new StompPrincipal(username));
                    log.info("WebSocket Interceptor: Authenticated user '{}'", username);
                }
            } else {
                // 如果没有认证信息，视业务需求，可以抛出异常拒绝连接
                // throw new IllegalArgumentException("未认证的用户");
                log.warn("WebSocket Interceptor: User tried to connect without username header");
            }
        }
        return message;
    }
}
