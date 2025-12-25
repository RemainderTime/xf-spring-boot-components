package com.xf.chat.model;

import lombok.*;

/**
 * 聊天消息实体类
 * <p>
 * 用于封装从客户端发送到服务器，或从服务器广播到客户端的消息内容。
 * 只使用基本的字段演示核心功能。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    /**
     * 消息类型 (CHAT, JOIN, LEAVE)
     */
    private MessageType type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送者 (用户昵称)
     */
    private String sender;

}
