package com.xf.chat.listener;

import com.xf.chat.model.ChatMessage;
import com.xf.chat.model.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

/**
 * WebSocket 事件监听器
 * <p>
 * 用于监听 Socket 连接和断开事件。
 * 特别是断开连接时，需要广播用户离开的消息。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    // SimpMessageSendingOperations 用于在代码中主动发送消息到指定 Topic
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 监听 Session 断开事件
     *
     * @param event 断开事件包含了 Session 信息
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("username");
            if (username != null) {
                log.info("User Disconnected : {}", username);

                // 构建一个 LEAVE 类型的消息
                ChatMessage chatMessage = ChatMessage.builder()
                        .type(MessageType.LEAVE)
                        .sender(username)
                        .build();

                // 广播消息到 /topic/public
                messagingTemplate.convertAndSend("/topic/public", chatMessage);
            }
        }
    }
}
