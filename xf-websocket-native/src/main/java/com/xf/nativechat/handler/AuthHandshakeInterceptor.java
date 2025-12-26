package com.xf.nativechat.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 握手拦截器
 * <p>
 * 在 WebSocket 建立连接之前（HTTP 升级阶段）拦截请求。
 * 作用：从 URL 参数中获取 token/uid，并进行简单的认证，如果不通过直接拒绝连接。
 */
@Component
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String uid = servletRequest.getServletRequest().getParameter("uid");

            if (uid != null && !uid.trim().isEmpty()) {
                // 将用户ID放入 Session Attributes 中，方便后续 Handler 使用
                attributes.put("uid", uid);
                log.info("Native WS Handshake success, uid: {}", uid);
                return true;
            }
        }
        log.warn("Native WS Handshake failed: missing uid");
        return false; // 返回 false 拒绝连接
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception) {
        // 握手之后，通常无需操作
    }
}
