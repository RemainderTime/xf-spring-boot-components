package com.xf.chat.controller;

import com.xf.chat.config.StompPrincipal;
import com.xf.chat.model.ChatMessage;
import com.xf.chat.model.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @InjectMocks
    private ChatController chatController;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @Test
    void testSendMessage() {
        // 准备数据
        ChatMessage message = ChatMessage.builder()
                .type(MessageType.CHAT)
                .content("Hello World")
                .sender("TestUser")
                .build();

        // 执行
        ChatMessage result = chatController.sendMessage(message);

        // 验证
        assertEquals(message, result);
    }

    @Test
    void testAddUser() {
        // 准备数据
        ChatMessage message = ChatMessage.builder()
                .type(MessageType.JOIN)
                .sender("NewUser")
                .build();

        // 模拟 WebSocket Session 属性
        Map<String, Object> sessionAttributes = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        // 创建一个模拟的 Principal
        StompPrincipal principal = new StompPrincipal("NewUser");

        // 执行
        ChatMessage result = chatController.addUser(message, headerAccessor, principal);

        // 验证
        assertEquals(message, result);
        // 验证用户名是否被正确放入 Session 中
        assertEquals("NewUser", sessionAttributes.get("username"));
    }
}
