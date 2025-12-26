package com.xf.chat.model;

import lombok.*;

/**
 * 聊天消息实体类
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

    /**
     * 接收者 (私聊时使用，用户昵称)
     */
    private String receiver;

}
