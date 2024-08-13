package com.example.mychat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, String> sessionUsers = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int onlineCount = 0;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 在连接建立时不做操作，等待接收用户ID
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 解析收到的消息
        Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) data.get("type");
        String userId = (String) data.get("userId");

        if ("join".equals(type)) {
            userSessions.put(userId, session);
            sessionUsers.put(session, userId);
            onlineCount++;
            broadcastOnlineCount();
            broadcastUserList();
        } else if ("private".equals(type)) {
            handlePrivateMessage(data,session);
        } else if ("broadcast".equals(type)) {
            handleBroadcastMessage(data, session);
        }
    }

    private void handlePrivateMessage(Map<String, Object> data, WebSocketSession senderSession) throws Exception {
        String contentType = (String) data.get("contentType");
        String targetUserId = (String) data.get("targetUserId");
        WebSocketSession targetSession = userSessions.get(targetUserId);

        if ("message".equals(contentType)) {
            String message = objectMapper.writeValueAsString(data);

            // 发送消息给目标用户
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.sendMessage(new TextMessage(message));
            }

            // 发送消息给自己
            if (senderSession != null && senderSession.isOpen()) {
                senderSession.sendMessage(new TextMessage(message));
            }
        }
    }


    private void handleBroadcastMessage(Map<String, Object> data, WebSocketSession senderSession) throws Exception {
        for (WebSocketSession session : userSessions.values()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));

        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = sessionUsers.get(session);
        if (userId != null) {
            userSessions.remove(userId);
            sessionUsers.remove(session);
            onlineCount--;
            broadcastOnlineCount();
            broadcastUserList();
        }
    }

    private void broadcastOnlineCount() throws Exception {
        Map<String, Object> jsonMessageMap = new HashMap<>();
        jsonMessageMap.put("type", "onlineCount");
        jsonMessageMap.put("count", onlineCount);

        String jsonMessage = objectMapper.writeValueAsString(jsonMessageMap);

        for (WebSocketSession session : userSessions.values()) {
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }

    private void broadcastUserList() throws Exception {
        List<String> onlineUsers = new ArrayList<>(userSessions.keySet());
        Map<String, Object> userListMessage = new HashMap<>();
        userListMessage.put("type", "userList");
        userListMessage.put("users", onlineUsers);
        String userListMessageStr = objectMapper.writeValueAsString(userListMessage);
        for (WebSocketSession session : userSessions.values()) {
            session.sendMessage(new TextMessage(userListMessageStr));
        }
    }
}
