package com.xf.nativechat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
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
    // JSON 转换工具
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立成功
     */
    /**
     * 更新心跳时间
     */
    private void updateLastHeartbeat(WebSocketSession session) {
        session.getAttributes().put("lastHeartbeat", System.currentTimeMillis());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();

        // 任何消息都算一次“活跃”，更新心跳时间
        updateLastHeartbeat(session);

        if ("ping".equalsIgnoreCase(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        String fromUid = (String) session.getAttributes().get("uid");

        // ... (JSON handling logic) ...
        try {
            com.fasterxml.jackson.databind.JsonNode json = objectMapper.readTree(payload);
            if (json.has("toUser") && json.has("content")) {
                String toUser = json.get("toUser").asText();
                String content = json.get("content").asText();
                String type = json.has("type") ? json.get("type").asText() : "text";

                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                msgMap.put("fromUser", fromUid);
                msgMap.put("content", content);
                msgMap.put("type", type);

                String forwardJson = objectMapper.writeValueAsString(msgMap);

                sendToUser(toUser, forwardJson);
                // 业务处理 异步/MQ 添加消息记录到数据库中

                session.sendMessage(new TextMessage("系统: 已发送 " + type + " 消息给 " + toUser));
                return;
            }
        } catch (Exception e) {
            log.debug("Not a standard JSON message: {}", e.getMessage());
        }

        log.info("Received from {}: {}", fromUid, payload);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String uid = (String) session.getAttributes().get("uid");
        if (uid != null) {
            USER_SESSION_MAP.put(uid, session);
            updateLastHeartbeat(session); // 初始化心跳时间
            log.info("User connected: {}", uid);
        }
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
     * 定时清理僵尸连接 (需要开启 @EnableScheduling)
     * 每 30 秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void cleanZombieSessions() {
        long now = System.currentTimeMillis();
        USER_SESSION_MAP.forEach((uid, session) -> {
            Long lastHeartbeat = (Long) session.getAttributes().get("lastHeartbeat");
            // 如果超过 60 秒没动静
            if (lastHeartbeat != null && (now - lastHeartbeat > 60000)) {
                try {
                    log.warn("Closing zombie session: {}", uid);
                    session.close(CloseStatus.SESSION_NOT_RELIABLE);
                } catch (IOException e) {
                    log.error("Close zombie session failed", e);
                }
            }
        });
    }

    /**
     * 业务方法：发送消息给指定用户
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
