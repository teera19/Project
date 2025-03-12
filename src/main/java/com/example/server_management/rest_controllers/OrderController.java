package com.example.server_management.rest_controllers;

import com.example.server_management.models.Order;
import com.example.server_management.models.User;
import com.example.server_management.repository.OrderRepository;
import com.example.server_management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/history")
    public ResponseEntity<?> getOrderHistory(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        User user = userRepository.findByUserName(userName);
        List<Order> orders = orderRepository.findByUserAndStatus(user, "PENDING"); // ✅ ดึงเฉพาะ Order ที่ยังไม่ยืนยัน
        return ResponseEntity.ok(orders);
    }
    @PatchMapping("/confirm-received/{orderId}")
    public ResponseEntity<?> confirmOrderReceived(HttpSession session, @PathVariable int orderId) {
        String userName = (String) session.getAttribute("user_name");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        User user = userRepository.findByUserName(userName);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().equals(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are not authorized to update this order"));
        }

        // ✅ อัปเดตสถานะคำสั่งซื้อเป็น "DELIVERED"
        order.setStatus("DELIVERED");
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("message", "Order marked as received", "orderId", orderId));
    }

}
