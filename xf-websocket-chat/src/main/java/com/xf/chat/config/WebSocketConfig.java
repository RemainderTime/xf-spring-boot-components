package com.xf.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 * <p>
 * 此类用于配置 WebSocket 连接和消息代理(Message Broker)。
 * 使用 Spring 的 STOMP (Simple Text Oriented Messaging Protocol) 支持。
 * <p>
 * 主要功能:
 * 1. 注册 STOMP 端点，供客户端连接。
 * 2. 配置消息代理，处理消息的路由。
 */
@Configuration
@EnableWebSocketMessageBroker // 开启 WebSocket 消息代理功能
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 注册 STOMP 协议的节点(Endpoint)，并映射到指定的 URL。
     * 客户端将连接到这个 URL 来建立 WebSocket 连接。
     *
     * @param registry STOMP 端点注册表
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个名为 "/ws" 的端点
        // setAllowedOriginPatterns("*") 用于允许跨域连接 (开发环境常用，生产环境建议指定具体域名)
        // withSockJS() 启用 SockJS 回退选项。如果浏览器不支持 WebSocket，将自动降级为轮询等机制。
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 配置消息代理 (Message Broker)
     * 用于决定消息是从 客户端 -> 服务器 还是 服务器 -> 客户端。
     *
     * @param registry 消息代理注册表
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 启用简单的内存消息代理 (Simple Broker)
        // 客户端订阅以 "/topic" 开头的地址时，消息代理会处理并将消息广播给所有订阅者。
        // 例如：客户端订阅 "/topic/public"，服务器往这里发消息，所有订阅者都能收到。
        registry.enableSimpleBroker("/topic");

        // 2. 设置应用前缀 (Application Destination Prefix)
        // 客户端发送消息给服务器时，如果目的地以 "/app" 开头，则会路由到 @MessageMapping 注解的方法中。
        // 例如：客户端发送到 "/app/chat.sendMessage"，会路由到 Controller 中
        // @MessageMapping("/chat.sendMessage") 的方法。
        registry.setApplicationDestinationPrefixes("/app");
    }
}
