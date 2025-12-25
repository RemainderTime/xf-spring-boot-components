'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
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

        // 1. 创建 SockJS 实例，连接到服务器端点 "/ws"
        var socket = new SockJS('/ws');

        // 2. 创建 STOMP 客户端
        stompClient = Stomp.over(socket);

        // 3. 建立连接
        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


/**
 * 连接成功回调
 */
function onConnected() {
    // 订阅 Public Topic
    // 任何发布到 "/topic/public" 的消息，我们都会在这个回调用收到
    stompClient.subscribe('/topic/public', onMessageReceived);

    // 告诉服务器我们加入了
    // 发送到 "/app/chat.addUser"，对应 Controller 的 @MessageMapping("/chat.addUser")
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({ sender: username, type: 'JOIN' })
    );

    // connectingElement.classList.add('hidden');    
}


/**
 * 连接错误回调
 */
function onError(error) {
    var status = document.querySelector('.status');
    if (status) {
        status.textContent = '无法连接到 WebSocket 服务器，请刷新页面重试。';
        status.style.color = 'red';
    }
    console.error('Could not connect to WebSocket server. Please refresh this page to try again!');
}


/**
 * 发送消息
 */
function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        // 发送到 "/app/chat.sendMessage"
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


/**
 * 接收并显示消息
 */
function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

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
