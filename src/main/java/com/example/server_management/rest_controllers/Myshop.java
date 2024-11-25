package com.example.server_management.rest_controllers;

import com.example.server_management.models.ProductResponse;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/")
public class Myshop {

    @Autowired
    private UserService userService;

    @PostMapping("/create-shop")
    public ResponseEntity<String> createShop(@RequestParam("user_name") String user_name,
                                             @RequestParam("title") String title,
                                             @RequestParam("detail") String detail) {

        if (user_name.isEmpty() || title.isEmpty() || detail.isEmpty()) {
            return new ResponseEntity<>("Please Complete all Fields", HttpStatus.BAD_REQUEST);
        }

        userService.createShopForUser(user_name, title, detail);
        return new ResponseEntity<>("Shop Created Successfully", HttpStatus.OK);
    }

    @PostMapping("/add-product")
    public ResponseEntity<String> addProduct(@RequestParam("shop_title") String shopTitle,
                                             @RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("price") double price,
                                             @RequestParam("image") MultipartFile image) {
        try {
            // ตรวจสอบว่าไฟล์มีขนาดไม่เกินที่กำหนด
            if (image.isEmpty()) {
                return new ResponseEntity<>("No image uploaded", HttpStatus.BAD_REQUEST);
            }

            // แปลงไฟล์ image เป็น byte[]
            byte[] imageBytes = image.getBytes();

            // เรียกใช้ userService เพื่อเพิ่มข้อมูลสินค้า พร้อมส่ง imageBytes
            userService.addProductToShop(shopTitle, name, description, price, imageBytes);

            return new ResponseEntity<>("Product added successfully", HttpStatus.OK);
        } catch (IOException e) {
            // การจัดการข้อผิดพลาดในกรณีที่เกิด IOException
            return new ResponseEntity<>("Failed to process the image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PatchMapping("/edit-product")
    public ResponseEntity<String> editProduct(@RequestParam("product_id") Integer productId,
                                              @RequestParam(value = "name", required = false) String name,
                                              @RequestParam(value = "description", required = false) String description,
                                              @RequestParam(value = "price", required = false) Double price,
                                              @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            userService.editProduct(productId, name, description, price, image);
            return new ResponseEntity<>("Product updated successfully", HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/all-product")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> response = userService.getAllProducts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/my-product")
    public ResponseEntity<List<ProductResponse>> getMyProducts(@RequestParam("user_name") String userName) {
        try {
            List<ProductResponse> response = userService.getMyProducts(userName);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

}
