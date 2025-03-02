package com.example.server_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // เปิดการใช้งาน message broker สำหรับการส่งข้อความไปที่ "/topic"
        config.enableSimpleBroker("/topic");
        // ใช้ prefix "/app" สำหรับแอปพลิเคชันที่ส่งข้อความ
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // เพิ่ม endpoint สำหรับการเชื่อมต่อ
        registry.addEndpoint("/chat").withSockJS();  // SockJS ใช้ fallback เมื่อ WebSocket ไม่สามารถใช้งานได้
    }
}
