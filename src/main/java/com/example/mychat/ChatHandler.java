package com.example.mychat;


import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 解析收到的消息
        Map<String, String> data = objectMapper.readValue(message.getPayload(), Map.class);
        String username = data.get("username");
        String userMessage = data.get("message");

        // 创建一个包含用户名和消息的 Map
        Map<String, String> jsonMessageMap = new HashMap<>();
        jsonMessageMap.put("username", username);
        jsonMessageMap.put("message", userMessage);

        // 将 Map 转换为 JSON 字符串
        String jsonMessage = objectMapper.writeValueAsString(jsonMessageMap);

        // 将消息广播给所有连接的客户端
        for (WebSocketSession webSocketSession : sessions) {
            webSocketSession.sendMessage(new TextMessage(jsonMessage));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
