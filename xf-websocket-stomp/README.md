# Spring Boot WebSocket 简易聊天室

这是一个基于 Spring Boot 3.3.3 和 WebSocket (STOMP协议) 实现的简易聊天室项目。
该项目演示了如何构建一个实时、双向通信的 Web 应用程序。

## 1. 项目结构说明

- **`com.xf.chat.config.WebSocketConfig`**: 核心配置类。
    -启用 STOMP 消息代理。
    - 配置 WebSocket 端点 (`/ws`)，支持 SockJS 回退。
    - 配置消息路由前缀 (`/app` -> 控制器, `/topic` -> 广播代理)。
- **`com.xf.chat.controller.ChatController`**: 消息控制器。
    - 处理客户端发送的消息 (`@MessageMapping`)。
    - 将处理后的消息发送给订阅者 (`@SendTo`)。
- **`com.xf.chat.listener.WebSocketEventListener`**: 事件监听器。
    - 监听 WebSocket 连接断开事件，自动广播用户离开的消息。
- **`com.xf.chat.model`**: 数据模型。
    - 定义消息格式 (`ChatMessage`) 和类型 (`MessageType`)。
- **frontend (`src/main/resources/static`)**: 前端实现。
    - 使用 SockJS 建立连接。
    - 使用 Stomp.js 处理消息订阅和发送。

## 2. 关键概念解析 (学习指南)

### WebSocket vs HTTP
HTTP 是请求-响应模式，服务器不能主动发消息给客户端。WebSocket 是全双工通信，建立连接后，服务器和客户端可以随时互相发送消息。

### STOMP (Simple Text Oriented Messaging Protocol)
原始的 WebSocket 只是一个通信通道。STOMP 定义了消息的语义（如 CONNECT, SEND, SUBSCRIBE）。Spring 使用 STOMP 来处理消息路由，类似于 Spring MVC 处理 HTTP 请求。

### Message Broker (消息代理)
在 `WebSocketConfig` 中配置的 `registry.enableSimpleBroker("/topic")` 是一个内存中的消息代理。
- 当 Controller 返回消息到 `/topic/...` 时，代理会把消息分发给所有订阅了该 Topic 的客户端。
- **生产级建议**: 在生产环境中（多实例集群部署），通常使用外部消息代理（如 RabbitMQ, ActiveMQ）代替内存代理，以实现不同服务器间的消息同步。

## 3. 运行方式

1. 确保已安装 JDK 17 和 Maven。
2. 在项目根目录运行:
   ```shell
   mvn spring-boot:run
   ```
3. 打开浏览器访问: `http://localhost:8080`
4. 打开多个浏览器窗口，输入不同昵称，即可互相聊天。

## 4. 生产环境扩展建议

虽然本项目实现了核心功能，但在生产环境中还需考虑：
1. **安全性**: 使用 Spring Security 保护 WebSocket 端点，验证用户身份 (Token 认证)。
2. **外部消息代理**: 使用 RabbitMQ/Kafka 处理高并发消息分发。
3. **消息持久化**: 将聊天记录保存到数据库 (MySQL/MongoDB)。
4. **私聊功能**: 使用 `@SendToUser` 实现一对一私聊。
5. **异常处理**: 完善错误捕获和前端重连机制。

## 5. 常见问题 (Troubleshooting)

### 'mvn' 命令报错: JAVA_HOME not defined
如果在运行 Maven 命令时遇到 `The JAVA_HOME environment variable is not defined correctly` 错误，说明您的操作系统环境变量中未设置 `JAVA_HOME`。

**解决方法**:
1.  找到您的 JDK 安装路径 (例如: `C:\Program Files\Java\jdk-17`).
2.  在系统环境变量中新建变量 `JAVA_HOME`，值为上述路径。
3.  编辑 `Path` 变量，添加 `%JAVA_HOME%\bin`。
4.  重启命令行窗口再次尝试。

## 6. 单元测试
本项目包含基础的 Context 加载测试和 Controller 单元测试。
运行测试:
```shell
mvn test
```
- `ChatApplicationTests`: 验证 Spring Context 是否能正常启动。
- `ChatControllerTest`: 验证消息处理逻辑和 Session 管理逻辑。

## 7. 深入学习
关于配置类、注解详解以及详细的消息发送流程，请查阅项目中的文档: [核心概念与流程解析](Docs.md)
