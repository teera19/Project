package com.example.server_management.config;


import com.example.server_management.handler.CustomWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CustomWebSocketHandler customWebSocketHandler;

    @Autowired
    public WebSocketConfig(CustomWebSocketHandler customWebSocketHandler) {
        this.customWebSocketHandler = customWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebSocketHandler, "/chat").setAllowedOrigins("*");
    }
}










