package com.xf.nativechat.config;

import com.xf.nativechat.handler.AuthHandshakeInterceptor;
import com.xf.nativechat.handler.MyNativeChatHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 原生 WebSocket 配置类
 * <p>
 * 注意这里使用 @EnableWebSocket 而不是 @EnableWebSocketMessageBroker
 * 这意味着我们没有 Broker，没有 STOMP，只有纯净的 WebSocket 通道。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class NativeWebSocketConfig implements WebSocketConfigurer {

    private final MyNativeChatHandler myNativeChatHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册处理器，并设置路径：/ws/native
        // setAllowedOriginPatterns("*") 允许跨域
        // addInterceptors 增加握手拦截器（用于鉴权）
        registry.addHandler(myNativeChatHandler, "/ws/native")
                .addInterceptors(authHandshakeInterceptor) // 握手拦截，提取 token
                .setAllowedOriginPatterns("*");
    }
}
