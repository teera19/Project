package com.example.server_management.rest_controllers;

import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/")
public class Myshop {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/create-shop", consumes = {"multipart/form-data"})
    public ResponseEntity<String> createShop(@RequestParam("user_name") String userName,
                                             @RequestParam("title") String title,
                                             @RequestParam("detail") String detail,
                                             @RequestParam("qr_code") MultipartFile qrCodeImage) {

        // ✅ ตรวจสอบฟิลด์ที่จำเป็น
        if (userName.isEmpty() || title.isEmpty() || detail.isEmpty() || qrCodeImage.isEmpty()) {
            return new ResponseEntity<>("Please complete all fields and upload QR Code", HttpStatus.BAD_REQUEST);
        }

        try {
            userService.createShopForUser(userName, title, detail, qrCodeImage);
            return new ResponseEntity<>("Shop Created Successfully with QR Code", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create shop: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
