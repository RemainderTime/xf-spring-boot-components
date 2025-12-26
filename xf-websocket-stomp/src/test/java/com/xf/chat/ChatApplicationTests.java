package com.xf.chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatApplicationTests {

    /**
     * 加载上下文测试
     * <p>
     * 这是一个基本的健康检查测试。
     * 如果 Spring 上下文无法成功启动（例如缺少 Bean，配置错误等），此测试将失败。
     * 这是确保应用程序能够运行的第一道防线。
     */
    @Test
    void contextLoads() {
    }

}
