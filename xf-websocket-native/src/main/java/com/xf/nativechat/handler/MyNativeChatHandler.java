package com.xf.nativechat.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 原生 WebSocket 处理器
 * <p>
 * 核心逻辑都在这里：管理连接、接收消息、发送消息。
 * 相比 STOMP，这里需要手动管理 Session Map。
 */
@Component
@Slf4j
public class MyNativeChatHandler extends TextWebSocketHandler {

    // 在线用户 Session 池
    // Key: uid, Value: Session
    private static final Map<String, WebSocketSession> USER_SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 连接建立成功
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String uid = (String) session.getAttributes().get("uid");
        if (uid != null) {
            USER_SESSION_MAP.put(uid, session);
            log.info("User connected: {}", uid);
            // 这里可以广播一个上线通知，或者啥都不做
        }
    }

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * 处理收到的文本消息
     * 支持两种格式：
     * 1. 纯文本 "ping" -> 回复 "pong"
     * 2. JSON 格式 { "toUser": "1002", "content": "你好" } -> 转发给 1002
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        log.info("消息体内同json: {}", payload);
        // 1. 心跳检测 (Ping-Pong)
        if ("ping".equalsIgnoreCase(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        String fromUid = (String) session.getAttributes().get("uid");

        // 2. 尝试解析为 JSON 并转发
        try {
            // 简单解析 JSON
            com.fasterxml.jackson.databind.JsonNode json = objectMapper.readTree(payload);
            if (json.has("toUser") && json.has("content")) {
                String toUser = json.get("toUser").asText();
                String content = json.get("content").asText();
                String type = json.has("type") ? json.get("type").asText() : "text";

                // 构造标准转发消息体
                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                msgMap.put("fromUser", fromUid);
                msgMap.put("content", content);
                msgMap.put("type", type);

                String forwardJson = objectMapper.writeValueAsString(msgMap);

                // 转发消息 (发送完整的 JSON 字符串)
                sendToUser(toUser, forwardJson);

                // 给自己回个执
                session.sendMessage(new TextMessage("系统: 已发送 " + type + " 消息给 " + toUser));
                return;
            }
        } catch (Exception e) {
            // 解析失败，说明不是 JSON，或者是普通文本
            log.debug("Not a standard JSON message: {}", e.getMessage());
        }

        // 3. 普通日志记录
        log.info("Received from {}: {}", fromUid, payload);
    }

    /**
     * 连接断开
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String uid = (String) session.getAttributes().get("uid");
        if (uid != null) {
            USER_SESSION_MAP.remove(uid);
            log.info("User disconnected: {}", uid);
        }
    }

    /**
     * 业务方法：发送消息给指定用户
     * (给 Controller 或 Service 调用的)
     */
    public void sendToUser(String uid, String message) {
        WebSocketSession session = USER_SESSION_MAP.get(uid);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Send message failed", e);
            }
        }
    }
}
