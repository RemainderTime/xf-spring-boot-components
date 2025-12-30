# xf-spring-boot-components

Spring Boot 组件整合示例项目集合。

## WebSocket 模块对比

本项目包含两个不同的 WebSocket 实现方案，适用于不同的业务场景：

### 1. xf-websocket-stomp (推荐用于即时通讯)
基于 **Spring Boot Starter WebSocket + STOMP 协议** 实现。
- **协议**：使用标准的 STOMP (Simple Text Oriented Messaging Protocol) 协议。
- **特点**：
  - 内置了消息代理（Broker），支持订阅（Subscribe）和广播（Broadcast）模式。
  - 自动处理消息路由（@MessageMapping）。
  - 支持 SockJS 降级策略（兼容旧浏览器）。
  - **适用场景**：复杂的即时通讯系统、群聊、需要精确订阅某个频道的消息推送。开发效率高，无需自己解析消息格式。

### 2. xf-websocket-native (原生模式)
基于 **Spring Native WebSocket (`TextWebSocketHandler`)** 实现。
- **协议**：直接操作底层的 WebSocket 文本/二进制帧，无任何上层封装。
- **特点**：
  - 极其轻量，连接建立就是一个纯 TCP 长连接。
  - 需要手动管理 Session 集合（`ConcurrentHashMap`）和心跳检测。
  - 需要自定义消息协议（如手动解析 JSON 的 `type`, `toUser` 字段）。
  - **适用场景**：对性能要求极高、不需要复杂订阅功能、或者只需要简单的点对点通知、需要自定义私有协议的场景（如游戏服务器、简单的设备心跳监控）。

## 快速开始
每个模块下均包含独立的 `Application` 启动类和前端测试页面 (`src/main/resources/static/index.html`)，可分别启动体验。
