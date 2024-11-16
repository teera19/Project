package com.example.server_management.rest_controllers;

import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class Myshop {

    // Inject UserService
    @Autowired
    private UserService userService;

    @PostMapping("/create-shop")
    public ResponseEntity<String> createShop(@RequestParam("user_name") String user_name,
                                             @RequestParam("title") String title,
                                             @RequestParam("detail") String detail) {

        // ตรวจสอบว่า fields ทั้งหมดไม่ว่างเปล่า
        if (user_name.isEmpty() || title.isEmpty() || detail.isEmpty()) {
            return new ResponseEntity<>("Please Complete all Fields", HttpStatus.BAD_REQUEST);
        }

        // สร้างร้านค้าสำหรับผู้ใช้
        userService.createShopForUser(user_name, title, detail);  // เรียกผ่าน instance ของ UserService

        return new ResponseEntity<>("Shop Created Successfully", HttpStatus.OK);
    }
}
