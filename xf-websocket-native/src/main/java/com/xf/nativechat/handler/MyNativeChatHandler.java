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

    /**
     * 处理收到的文本消息
     * 前端发: ws.send("hello")
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();

        // 1. 心跳检测 (Ping-Pong)
        // 收到 "ping" 立刻回 "pong"，不打印日志，避免刷屏
        if ("ping".equalsIgnoreCase(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        // 2. 正常业务消息处理
        log.info("Received from {}: {}", session.getAttributes().get("uid"), payload);
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
