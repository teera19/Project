package com.example.server_management.rest_controllers;

import com.example.server_management.dto.ChatRequest;
import com.example.server_management.dto.MessageRequest;
import com.example.server_management.models.ChatRoom;
import com.example.server_management.models.Message;
import com.example.server_management.service.ChatService;
import com.example.server_management.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class Chat {
    @Autowired
    private ChatService chatService;
    @Autowired
    private ProductService productService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/start") //ปุ่มเริ่มแชทในราลเอียดสินค้า
    public ResponseEntity<Map<String, Object>> startChat(
            @SessionAttribute("user_name") String user1,
            @RequestBody ChatRequest chatRequest //  ใช้ @RequestBody เพื่อรับ JSON Body
    ) {
        int productId = chatRequest.getProductId();

        // ค้นหาเจ้าของสินค้า (user2) จากฐานข้อมูล
        String user2 = productService.findSellerByProductId(productId);
        if (user2 == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // เช็คว่ามีห้องแชทอยู่แล้วหรือไม่
        ChatRoom chatRoom = chatService.getOrCreateChatRoom(user1, user2, productId);

        // ส่ง `chatId` กลับไปให้ Frontend
        Map<String, Object> response = new HashMap<>();
        response.put("chatId", chatRoom.getChatId());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{chatId}/send")
    public ResponseEntity<Message> sendMessage(
            @SessionAttribute("user_name") String sender,
            @PathVariable int chatId,
            @RequestBody MessageRequest request
    ) {
        // ตรวจสอบสิทธิ์ในการส่งข้อความ
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);
        if (!chatRoom.getUser1().equals(sender) && !chatRoom.getUser2().equals(sender)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // ส่งข้อความ
        Message message = chatService.sendMessage(chatId, sender, request.getMessage());

        // เมื่อส่งข้อความแล้ว, แจ้งเตือนผู้ใช้คนอื่นในห้องแชทนี้
        if (chatRoom.getUser1().equals(sender)) {
            // ส่งการแจ้งเตือนไปยังผู้ใช้ที่เป็น User2
            messagingTemplate.convertAndSendToUser(chatRoom.getUser2(), "/topic/messages", message);
        } else {
            // ส่งการแจ้งเตือนไปยังผู้ใช้ที่เป็น User1
            messagingTemplate.convertAndSendToUser(chatRoom.getUser1(), "/topic/messages", message);
        }

        return ResponseEntity.ok(message);
    }

    // ตัวอย่างการดึงประวัติแชท
    @GetMapping("/{chatId}/history")
    public ResponseEntity<?> getChatHistory(
            @SessionAttribute("user_name") String currentUser,
            @PathVariable int chatId
    ) {
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);

        // ตรวจสอบสิทธิ์การเข้าถึง
        if (!chatRoom.getUser1().equals(currentUser) && !chatRoom.getUser2().equals(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("คุณไม่มีสิทธิ์ดูแชทนี้");
        }

        List<Message> messages = chatService.getChatHistory(chatId);
        return ResponseEntity.ok(messages);
    }
    @GetMapping("/my-chats")
    public ResponseEntity<List<Map<String, Object>>> getMyChats(@SessionAttribute("user_name") String currentUser) {
        List<ChatRoom> chatRooms = chatService.getChatsByUser(currentUser);

        List<Map<String, Object>> response = chatRooms.stream().map(chatRoom -> {
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("chatId", chatRoom.getChatId());
            chatData.put("user1", chatRoom.getUser1());
            chatData.put("user2", chatRoom.getUser2());
            chatData.put("productId", chatRoom.getProductId());

            // ✅ ส่งข้อความล่าสุด (ถ้ามี)
            if (chatRoom.getLatestMessage() != null) {
                chatData.put("latestMessage", chatRoom.getLatestMessage().getMessage());
                chatData.put("latestSender", chatRoom.getLatestMessage().getSender());
                chatData.put("latestTime", chatRoom.getLatestMessage().getCreatedAt());
            } else {
                chatData.put("latestMessage", "ไม่มีข้อความ");
            }

            return chatData;
        }).toList();

        return ResponseEntity.ok(response);
    }


}