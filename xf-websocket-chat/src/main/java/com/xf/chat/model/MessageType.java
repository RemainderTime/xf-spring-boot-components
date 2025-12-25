package com.xf.chat.model;

/**
 * 消息类型枚举
 * <p>
 * CHAT: 普通聊天消息
 * JOIN:以此类型标识用户加入
 * LEAVE: 以此类型标识用户离开
 */
public enum MessageType {
    CHAT,
    JOIN,
    LEAVE
}
