package com.example.server_management.rest_controllers;

import com.example.server_management.models.Order;
import com.example.server_management.models.User;
import com.example.server_management.repository.OrderRepository;
import com.example.server_management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        User user = userRepository.findByUserName(userName);
        List<Order> orders = orderRepository.findByUser(user);
        return ResponseEntity.ok(orders);
    }
}
