package com.example.server_management.rest_controllers;

import com.example.server_management.service.CartService;
import com.example.server_management.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ChatService chatService;

    @GetMapping(value = "/count",produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, Integer>> getNotificationCount(@SessionAttribute("user_name") String userName) {
        // ✅ ดึงจำนวนสินค้าในตะกร้า
        int cartCount = cartService.getCartItemCount(userName);

        // ✅ ดึงจำนวนข้อความที่ยังไม่ได้อ่าน
        int unreadMessages = chatService.getUnreadMessageCount(userName);

        // ✅ ส่งค่ากลับไปให้ Frontend
        return ResponseEntity.ok(Map.of(
                "cartCount", cartCount,
                "unreadMessages", unreadMessages
        ));
    }
}
