package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/")
public class Addproduct {

    @Autowired
    private UserService userService;

    @PostMapping("/add-product")
    public ResponseEntity<Object> addProduct(@RequestParam("shop_title") String shopTitle,
                                             @RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("price") double price,
                                             @RequestParam("image") MultipartFile image,
                                             @RequestParam("category_id") int categoryId) throws IOException {

        try {
            if (image.isEmpty()) {
                return new ResponseEntity<>("No image uploaded", HttpStatus.BAD_REQUEST);
            }

            // แปลงไฟล์ image เป็น byte[]
            byte[] originalImageBytes = image.getBytes();

            // บีบอัดภาพ
            byte[] compressedImageBytes = compressImage(originalImageBytes);

            // เรียกใช้ userService เพื่อเพิ่มข้อมูลสินค้า
            Product addedProduct = userService.addProductToShop(shopTitle, name, description, price, compressedImageBytes, categoryId);

            return new ResponseEntity<>(addedProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ฟังก์ชันสำหรับบีบอัดภาพ
    private byte[] compressImage(byte[] imageBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (originalImage == null) {
            throw new IOException("Cannot read the image from the provided byte array");
        }

        // กำหนดขนาดของภาพที่ต้องการ
        int targetWidth = 100; // ความกว้างของภาพที่ลดขนาด
        int targetHeight = (int) (originalImage.getHeight() * (100.0 / originalImage.getWidth()));

        // ลดขนาดภาพ
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

        BufferedImage bufferedScaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedScaledImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // แปลงภาพเป็น byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedScaledImage, "jpg", baos);
        baos.flush();
        return baos.toByteArray();
    }
}

