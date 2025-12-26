# Spring Boot WebSocket 核心概念与流程解析

本文档旨在详细解释本项目中 WebSocket 实现的核心组件、注解及消息流转流程。

## 1. 核心组件解析

### 1.1 `WebSocketConfig` (配置类) - **必须**
这是 WebSocket 的**基础设施**。没有它，WebSocket 服务无法启动。
*   **`@EnableWebSocketMessageBroker`**: 这个注解开启了基于 STOMP 协议的消息代理功能。它告诉 Spring Boot：“嘿，我要用 WebSocket 发送信使（STOMP）消息了，请帮我处理消息路由”。
*   **`registerStompEndpoints`**:
    *   **作用**: 定义“大门”。客户端（前端）建立连接时，必须先敲这个门。
    *   **代码**: `registry.addEndpoint("/ws").withSockJS();`
    *   **解释**: 客户端 JS 代码 `new SockJS('/ws')` 就是连接到这里。`withSockJS()` 是为了兼容性，如果浏览器不支持原生 WebSocket，它会自动降级使用 HTTP 轮询。
*   **`configureMessageBroker`**:
    *   **作用**: 定义“邮局”的规则。
    *   **`enableSimpleBroker("/topic")`**: 启用一个内存中的消息代理。所有发往 `/topic/...` 的消息，代理都会负责分发给订阅了该地址的客户端。
    *   **`setApplicationDestinationPrefixes("/app")`**: 定义应用前缀。所有发往 `/app/...` 的消息，会被 Spring 路由到你的 `Controller` 中处理。

### 1.2 `WebSocketEventListener` (监听类) - **非必须，但强烈建议**
*   **作用**: 监听 Socket 的生命周期事件（连接成功、连接断开）。
*   **场景**: 在聊天室中，**必须要**这个类，因为 HTTP 请求是无状态的，但 WebSocket 连接是有状态的。当用户直接关闭浏览器时，服务器需要感知到这个“断开”事件，从而广播“某某离开了”的消息。如果不加这个类，用户走了，其他人不知道。

## 2. Controller 注解详解

`ChatController` 类似于 Web 开发中的 `RestController`，但它处理的是 STOMP 消息帧，而不是 HTTP 请求。

*   **`@MessageMapping("/chat.sendMessage")`**:
    *   **对应**: 对应客户端发送的 `stompClient.send("/app/chat.sendMessage", ...)`。
    *   **注意**: 客户端写的路径是 `/app` + 注解路径。
    *   **作用**: 当收到这条消息时，执行该方法。
*   **`@SendTo("/topic/public")`**:
    *   **作用**: 方法的**返回值**会自动发送到这个指定的 Topic。
    *   **流程**: 方法收到消息 -> 处理业务逻辑 -> `return msg` -> 自动广播到 `/topic/public`。
    *   **等价代码**: 如果不写这个注解，你需要在方法内部使用 `SimpMessageSendingOperations.convertAndSend("/topic/public", msg)` 手动发送。
*   **`@Payload`**:
    *   **作用**: 提取消息体（Body）的内容，并绑定到方法参数对象（`ChatMessage`）上。类似于 Spring MVC 的 `@RequestBody`。
*   **`SimpMessageHeaderAccessor`**:
    *   **作用**: 用于获取 WebSocket 的 Session 信息。因为 WebSocket 是长连接，我们可以把用户信息（如用户名）存在 Session 属性里，以便在后续（如断开连接时）取出来使用。

## 3. 消息发送流程图解

假设用户 A (UserA) 发送了一条消息 "Hello"：

**步骤 1: 建立连接 (Handshake)**
*   UserA 的浏览器通过 JS 发起连接: `connect("/ws")`。
*   连接建立成功。

**步骤 2: 订阅频道 (Subscribe)**
*   UserA 的 JS 代码执行: `stompClient.subscribe('/topic/public')`。
*   UserB, UserC 也执行了同样的操作。
*   现在，大家都在监听 `/topic/public` 这个频道。

**步骤 3: 发送消息 (Send)**
*   UserA 输入 "Hello"，JS 执行发送:
    ```javascript
    stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({sender: "UserA", content: "Hello"}));
    ```
    *   注意路径是 `/app/...`，所以消息直接飞向服务器的 `Controller`。

**步骤 4: 服务器处理 (Routing & Processing)**
*   Spring 检测到前缀 `/app`，将消息路由到 `ChatController`。
*   根据路径 `/chat.sendMessage`，找到 `sendMessage` 方法。
*   方法执行，返回 `ChatMessage` 对象。

**步骤 5: 广播 (Broadcasting)**
*   因为方法上有 `@SendTo("/topic/public")`，Spring 的**消息代理 (Broker)** 接管返回值。
*   Broker 查找所有订阅了 `/topic/public` 的用户 (UserA, UserB, UserC)。
*   Broker 将消息推送到他们的 WebSocket 连接中。

**步骤 6: 接收展示 (Receive)**
*   UserA, UserB, UserC 的浏览器收到消息。
*   前端 JS 的 `onMessageReceived` 回调函数被触发。
*   JS 解析消息，将 "UserA: Hello" 渲染到屏幕上。
