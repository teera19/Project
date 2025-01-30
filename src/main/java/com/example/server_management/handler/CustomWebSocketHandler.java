package com.example.server_management.handler;

import com.example.server_management.models.Message;
import com.example.server_management.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {
    @Autowired
    private UserRepository userRepository;

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // ดึง user_name จาก Query String
        String userName = session.getUri().getQuery().split("=")[1];

        // ตรวจสอบ user_name ในฐานข้อมูล
        if (!isValidUserName(userName)) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // เก็บ user_name กับ WebSocketSession
        sessions.put(userName, session);
        System.out.println("User connected: " + userName);
    }

    private boolean isValidUserName(String userName) {
        // ตรวจสอบ user_name จากฐานข้อมูล
        return userRepository.existsByUserName(userName);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // รับข้อความจาก Client
        System.out.println("Received: " + message.getPayload());
        ObjectMapper mapper = new ObjectMapper();
        Message chatMessage = mapper.readValue(message.getPayload(), Message.class);

        // ส่งข้อความไปยัง Receiver
        WebSocketSession receiverSession = sessions.get(chatMessage.getReceiver());
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(mapper.writeValueAsString(chatMessage)));
        } else {
            System.out.println("Receiver not found or disconnected");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        // ลบ session ออกจาก Map
        sessions.values().remove(session);
        System.out.println("User disconnected: " + session.getId());
    }
}
