package com.example.server_management.rest_controllers;

import com.example.server_management.dto.MessageRequest;
import com.example.server_management.dto.MessageRespons;
import com.example.server_management.models.Message;
import com.example.server_management.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class Chat {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send/{productId}")
    public ResponseEntity<?> sendMessage(
            @PathVariable int productId,  // รับ productId จาก URL
            @RequestBody MessageRequest request) {
        try {
            Message message = messageService.sendMessage(
                    request.getSender(),
                    request.getReceiver(),
                    request.getContent(),
                    productId // ใช้ productId จาก Path
            );
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/conversation/{user1}/{user2}")
    public ResponseEntity<?> getConversation(
            @PathVariable("user1") String user1,
            @PathVariable("user2") String user2) {
        try {
            List<Message> messages = messageService.getConversation(user1, user2);

            if (messages == null || messages.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            List<MessageRespons> response = messages.stream()
                    .map(MessageRespons::new)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();  // ดู log เพิ่มเติม
            return new ResponseEntity<>(Map.of("error", "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}










