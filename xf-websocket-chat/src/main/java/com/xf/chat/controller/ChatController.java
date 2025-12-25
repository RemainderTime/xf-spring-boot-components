package com.xf.chat.controller;

import com.xf.chat.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

/**
 * 聊天控制器
 * <p>
 * 处理 WebSocket 消息的路由和逻辑。
 * 类似于 Spring MVC 的 @RestController，但使用的是 @MessageMapping 处理 STOMP 消息。
 */
@Controller
public class ChatController {

    /**
     * 处理发送消息的请求
     * <p>
     * 客户端发送目标: /app/chat.sendMessage (因为 Config 中配置了 /app 前缀)
     * 消息处理后转发目标: /topic/public (广播给所有订阅者)
     *
     * @param chatMessage 消息内容
     * @return 转发给订阅者的消息
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // 在此处可以添加业务逻辑，例如消息持久化到数据库
        return chatMessage;
    }

    /**
     * 处理用户加入的请求
     * <p>
     * 客户端发送目标: /app/chat.addUser
     * 转发目标: /topic/public
     *
     * @param chatMessage    包含用户名的消息
     * @param headerAccessor WebSocket 头信息访问器，用于在 Session 中存储用户名
     * @return 加入通知消息
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // 将用户名存储在 WebSocket Session 中，以便在用户断开连接时使用
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", chatMessage.getSender());
        return chatMessage;
    }

}
