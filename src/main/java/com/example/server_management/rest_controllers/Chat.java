package com.example.server_management.rest_controllers;

import com.example.server_management.dto.MessageRequest;
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

@RestController
@RequestMapping("/chat")
public class Chat {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request) {
        try {
            // ส่งข้อความผ่าน MessageService
            Message message = messageService.sendMessage(
                    request.getSender(), request.getReceiver(), request.getContent());
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // กรณีข้อมูลผู้ส่งหรือผู้รับไม่ถูกต้อง
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // กรณีเกิดข้อผิดพลาดอื่น ๆ
            return new ResponseEntity<>(Map.of("error", "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/conversation/{user1}/{user2}")
    public ResponseEntity<List<Message>> getConversation(
            @PathVariable("user1") String user1,
            @PathVariable("user2") String user2) {

        List<Message> messages = messageService.getConversation(user1, user2);

        if (messages.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}








