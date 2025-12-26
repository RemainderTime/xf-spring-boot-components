package com.xf.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 移除 withSockJS()
        // 这样对外暴露的就是标准的 WebSocket 协议 (ws://domain/ws)，所有客户端(Web/App/小程序)都能直连
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // 允许跨域
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 增加 "/queue" 用于点对点消息
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        // 指定用户点对点消息的前缀，默认为 "/user"
        // 客户端订阅地址: /user/queue/private (Spring 会自动转换为 /queue/private-user{session})
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 配置客户端入站通道拦截器
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
