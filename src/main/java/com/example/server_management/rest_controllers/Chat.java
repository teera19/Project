package com.example.server_management.rest_controllers;

import com.example.server_management.component.ChatStatusTracker;
import com.example.server_management.dto.ChatRequest;
import com.example.server_management.dto.MessagePayload;
import com.example.server_management.dto.MessageRequest;
import com.example.server_management.models.ChatRoom;
import com.example.server_management.models.Message;
import com.example.server_management.repository.MessageRepository;
import com.example.server_management.service.ChatService;
import com.example.server_management.service.ProductService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    @Autowired
    private ChatStatusTracker chatStatusTracker;
    @Autowired
    private MessageRepository messageRepository;


    @PostMapping(value = "/start",produces = "application/json;charset=UTF-8") //ปุ่มเริ่มแชทในราลเอียดสินค้า
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


    @PostMapping(value = "/{chatId}/send",produces = "application/json;charset=UTF-8")
    public ResponseEntity<Message> sendMessage(
            @SessionAttribute("user_name") String sender,
            @PathVariable int chatId,
            @RequestBody MessageRequest request) {



        ChatRoom chatRoom = chatService.getChatRoomById(chatId);
        if (!chatRoom.getUser1().equals(sender) && !chatRoom.getUser2().equals(sender)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // ✅ สร้างข้อความใหม่
        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setSender(sender);  // ✅ บังคับให้มี sender เสมอ
        message.setMessage(request.getMessage());
        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        message.setRead(false);

        // ✅ บันทึกลง Database
        messageRepository.save(message);

        // ✅ หาผู้รับของข้อความ
        String receiver = chatRoom.getOtherUser(sender);

        // ✅ บังคับให้ WebSocket ส่ง JSON เสมอ
        // ✅ สร้าง Object แทน Map<String, Object>
        MessagePayload payload = new MessagePayload(
                chatId,
                message.getMessageId(),
                sender != null ? sender : "Unknown",
                message.getMessage(),
                message.getCreatedAt().toInstant()
        );

// ✅ ส่งตรงไปยัง WebSocket (Spring แปลงเป็น JSON อัตโนมัติ)
        messagingTemplate.convertAndSendToUser(receiver, "/topic/messages", payload);

        // ✅ อัปเดตตัวเลขแจ้งเตือนของผู้รับ
        int unreadMessages = chatService.getUnreadMessageCount(receiver);
        messagingTemplate.convertAndSendToUser(receiver, "/topic/unread-messages", Map.of(
                "unreadMessages", unreadMessages
        ));

        return ResponseEntity.ok(message);
    }

    // ตัวอย่างการดึงประวัติแชท
    @GetMapping(value = "/{chatId}/history",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getChatHistory(@SessionAttribute("user_name") String currentUser, @PathVariable int chatId) {
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);

        if (!chatRoom.getUser1().equals(currentUser) && !chatRoom.getUser2().equals(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("คุณไม่มีสิทธิ์ดูแชทนี้");
        }

        List<Message> messages = chatService.getChatHistory(chatId);

        // ✅ ตรวจสอบ sender โดยไม่บันทึกกลับ DB
//        messages.forEach(message -> {
//            if (message.getSender() == null || message.getSender().isEmpty()) {
//                message.setSender("Unknown");  // ✅ ป้องกัน sender เป็น null
//            }
//        });

        return ResponseEntity.ok(messages);
    }

    @GetMapping(value = "/my-chats",produces = "application/json;charset=UTF-8")
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

    @PostMapping(value = "/active",produces = "application/json;charset=UTF-8")
    public ResponseEntity<Void> setActiveChat(@SessionAttribute("user_name") String username,
                                              @RequestBody Map<String, Integer> request) {

        int chatId = request.get("chatId");  // รับ chatId จาก request body
        chatStatusTracker.setActiveChat(username, chatId);  // บันทึกสถานะ active ของผู้ใช้
        return ResponseEntity.ok().build();  // ส่ง HTTP 200 OK
    }

    // สำหรับเคลียร์สถานะ inactive เมื่อผู้ใช้ออกจากห้องแชท
    @PostMapping(value = "/inactive",produces = "application/json;charset=UTF-8")
    public ResponseEntity<Void> clearActiveChat(@SessionAttribute("user_name") String username) {
        chatStatusTracker.clearActiveChat(username);  // เคลียร์สถานะ active ของผู้ใช้
        return ResponseEntity.ok().build();  // ส่ง HTTP 200 OK
    }

}