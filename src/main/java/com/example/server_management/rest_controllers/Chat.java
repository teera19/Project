package com.example.server_management.rest_controllers;

import com.example.server_management.dto.ChatRequest;
import com.example.server_management.dto.ChatRoomDTO;
import com.example.server_management.dto.MessageRequest;
import com.example.server_management.models.ChatRoom;
import com.example.server_management.models.Message;
import com.example.server_management.service.ChatService;
import com.example.server_management.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startChat(
            HttpSession session, // ใช้ HttpSession แทน
            @RequestBody ChatRequest chatRequest
    ) {
        String user1 = (String) session.getAttribute("user_name");

        if (user1 == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "User not logged in. Please log in first."
            ));
        }

        int productId = chatRequest.getProductId();
        String user2 = productService.findSellerByProductId(productId);

        if (user2 == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        ChatRoom chatRoom = chatService.getOrCreateChatRoom(user1, user2, productId);

        return ResponseEntity.ok(Map.of("chatId", chatRoom.getChatId()));
    }



    @GetMapping("/{chatId}/history")
    public ResponseEntity<?> getChatHistory(
            HttpSession session, // ใช้ HttpSession
            @PathVariable int chatId
    ) {
        String currentUser = (String) session.getAttribute("user_name");

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in.");
        }

        ChatRoom chatRoom = chatService.getChatRoomById(chatId);

        if (!chatRoom.getUser1().equals(currentUser) && !chatRoom.getUser2().equals(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("คุณไม่มีสิทธิ์ดูแชทนี้ ❌");
        }

        List<Message> messages = chatService.getChatHistory(chatId);
        return ResponseEntity.ok(messages);
    }


    @PostMapping("/{chatId}/send")
    public ResponseEntity<Message> sendMessage(
            HttpSession session,
            @PathVariable int chatId,
            @RequestBody MessageRequest request
    ) {
        String sender = (String) session.getAttribute("user_name");

        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        ChatRoom chatRoom = chatService.getChatRoomById(chatId);

        if (!chatRoom.getUser1().equals(sender) && !chatRoom.getUser2().equals(sender)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Message message = chatService.sendMessage(chatId, sender, request.getMessage());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/my-chats")
    public ResponseEntity<List<ChatRoomDTO>> getMyChats(HttpSession session) {
        String currentUser = (String) session.getAttribute("user_name");

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<ChatRoom> chatRooms = chatService.getChatsByUser(currentUser);

        List<ChatRoomDTO> chatRoomDTOs = chatRooms.stream().map(chatRoom -> {
            Message latestMessage = chatService.getLatestMessage(chatRoom.getChatId());
            return new ChatRoomDTO(
                    chatRoom,
                    latestMessage != null ? latestMessage.getMessage() : "ไม่มีข้อความ",
                    latestMessage != null ? latestMessage.getTimestamp().toString() : ""
            );
        }).toList();

        return ResponseEntity.ok(chatRoomDTOs);
    }

}

