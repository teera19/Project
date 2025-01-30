package com.example.server_management.rest_controllers;

import com.example.server_management.models.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;

@Controller
public class Chat{
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
}







