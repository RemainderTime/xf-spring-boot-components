package com.xf.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用程序启动类
 * <p>
 * 如需更多学习资源，建议查阅 Spring Framework 官方文档中关于 WebSocket 的章节。
 * https://docs.spring.io/spring-framework/reference/web/websocket.html
 */
@SpringBootApplication
public class ChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

}
