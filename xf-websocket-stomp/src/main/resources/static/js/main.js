'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var chatTitle = document.querySelector('#chat-title');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var receiverInput = document.querySelector('#receiver');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

/**
 * 连接 WebSocket
 */
function connect(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');
        chatTitle.textContent = "Welcome, " + username;

        // 1. 使用原生 WebSocket 构造函数 (不再依赖 sockjs.js)
        // 注意：Web端必须写完整的 ws:// 或 wss:// 协议头
        // 这里的路径 '/ws' 对应后端 registry.addEndpoint("/ws")

        // 自动检测协议 (https -> wss, http -> ws)
        var protocol = location.protocol === 'https:' ? 'wss://' : 'ws://';
        var wsUrl = protocol + location.host + '/ws';

        stompClient = Stomp.client(wsUrl);

        // 可选：开启心跳检测 (每10秒一次)
        stompClient.heartbeat.outgoing = 10000;
        stompClient.heartbeat.incoming = 10000;

        // 2. 在 Connect Headers 中传递认证信息 (username)
        // 生产环境中这里通常传 { 'Authorization': 'Bearer ' + token }
        stompClient.connect({ username: username }, onConnected, onError);
    }
    event.preventDefault();
}


/**
 * 连接成功回调
 */
function onConnected() {
    // 订阅群聊 (Public Topic)
    stompClient.subscribe('/topic/public', onMessageReceived);

    // 订阅私聊 (User Specific Queue)
    // 客户端只需要订阅 "/user/queue/private"，Spring 会自动转换
    stompClient.subscribe('/user/queue/private', onPrivateMessageReceived);

    // 告诉服务器我们加入了 (群聊广播)
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({ sender: username, type: 'JOIN' })
    );
}


/**
 * 连接错误回调
 */
function onError(error) {
    var status = document.querySelector('.status');
    if (status) { // defensive check
        status.textContent = '无法连接到 WebSocket 服务器，请刷新页面重试。';
        status.style.color = 'red';
    }
    console.error('Could not connect to WebSocket server:', error);
}


/**
 * 发送消息
 */
function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    var receiver = receiverInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        if (receiver && receiver.length > 0) {
            // 私聊逻辑
            chatMessage.receiver = receiver;
            // 发送到私聊 endpoint
            stompClient.send("/app/chat.private", {}, JSON.stringify(chatMessage));

            // 因为私聊发给自己不会被广播回来(除非发给自己)，所以需要手动显示在界面上
            showSelfMessage(chatMessage);
        } else {
            // 群聊逻辑
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        }

        messageInput.value = '';
    }
    event.preventDefault();
}

/**
 * 显示自己发的私聊消息
 */
function showSelfMessage(message) {
    var messageElement = document.createElement('li');
    messageElement.classList.add('chat-message');
    messageElement.classList.add('self'); // 使用自己的样式

    // 可以加个标记说是私聊
    message.content = "(私聊 " + message.receiver + ") " + message.content;

    var avatarElement = document.createElement('i');
    var avatarText = document.createTextNode(message.sender[0]);
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(message.sender);
    avatarElement.classList.add('avatar');
    messageElement.appendChild(avatarElement);

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);
    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


/**
 * 接收并显示群聊消息
 */
function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    displayMessage(message);
}

/**
 * 接收并显示私聊消息
 */
function onPrivateMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    message.content = "(私信) " + message.content; // 区分私信
    displayMessage(message);
}

/**
 * 通用渲染消息逻辑
 */
function displayMessage(message) {
    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' 加入了聊天!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' 离开了聊天!';
    } else {
        if (message.sender === username) {
            messageElement.classList.add('chat-message');
            messageElement.classList.add('self');
        } else {
            messageElement.classList.add('chat-message');
        }

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);
        avatarElement.classList.add('avatar');
        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        usernameElement.classList.add('username');
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


/**
 * 根据用户名获取头像颜色
 */
function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
