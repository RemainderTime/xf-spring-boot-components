package com.xf.chat.controller;

import com.xf.chat.model.ChatMessage;
import com.xf.chat.model.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    /**
     * 处理私聊消息
     * 发送目标: /app/chat.private
     */
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, Principal principal) {
        String receiver = chatMessage.getReceiver();
        // 设置发送者为当前 Principal 的名字，防止伪造
        chatMessage.setSender(principal.getName());
        chatMessage.setType(MessageType.CHAT);

        log.info("Private message from {} to {}", principal.getName(), receiver);

        // 发送给接收者
        // 目标地址由 UserDestinationPrefix (/user) + receiver + destination (/queue/private)
        // 组成
        // 最终接收者订阅的地址是: /user/queue/private
        messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/private",
                chatMessage);

        // 可选: 也发给自己一份（为了在界面上显示自己发出的私聊消息）
        // 或者前端直接在发送成功后渲染
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        // 其实有了 HandshakeHandler，Principal 已经在 Session 里了
        // 这里如果是为了兼容旧逻辑或者做 double check
        if (principal != null) {
            log.info("User connected: {}", principal.getName());
            Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", principal.getName());
        }
        return chatMessage;
    }

}
