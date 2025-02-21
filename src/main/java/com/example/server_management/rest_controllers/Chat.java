package com.example.server_management.rest_controllers;

import com.example.server_management.dto.ChatRequest;
import com.example.server_management.dto.MessageRequest;
import com.example.server_management.models.ChatRoom;
import com.example.server_management.models.Message;
import com.example.server_management.service.ChatService;
import com.example.server_management.service.ProductService;
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


    @GetMapping("/{chatId}/history") //ประวัติแชท
    public ResponseEntity<?> getChatHistory(
            @SessionAttribute("user_name") String currentUser, //  ดึง user จาก session
            @PathVariable int chatId
    ) {
        // ดึงห้องแชทจากฐานข้อมูล
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);

        //  ตรวจสอบว่าผู้ใช้ที่ขอเป็น `user1` หรือ `user2` หรือไม่
        if (!chatRoom.getUser1().equals(currentUser) && !chatRoom.getUser2().equals(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("คุณไม่มีสิทธิ์ดูแชทนี้ ");
        }

        //  ถ้าผ่านการตรวจสอบ ดึงประวัติแชท
        List<Message> messages = chatService.getChatHistory(chatId);
        return ResponseEntity.ok(messages);
    }


    @PostMapping("/{chatId}/send") //ส่งข้อความ
    public ResponseEntity<Message> sendMessage(
            @SessionAttribute("user_name") String sender, // ดึง sender จาก Session
            @PathVariable int chatId,
            @RequestBody MessageRequest request //  รับเฉพาะ message จาก Body
    ) {
        //  ตรวจสอบว่า sender มีสิทธิ์ส่งข้อความในห้องแชทนี้หรือไม่
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);
        if (!chatRoom.getUser1().equals(sender) && !chatRoom.getUser2().equals(sender)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // ส่งข้อความ
        Message message = chatService.sendMessage(chatId, sender, request.getMessage());
        return ResponseEntity.ok(message);
    }
    @GetMapping("/my-chats") //ปุ่มไอค่อนรูปข้อความ
    public ResponseEntity<List<ChatRoom>> getMyChats(@SessionAttribute("user_name") String currentUser) {
        List<ChatRoom> chatRooms = chatService.getChatsByUser(currentUser);
        return ResponseEntity.ok(chatRooms);
    }

}