package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/")
public class Myproduct {

    @Autowired
    private UserService userService;

    @GetMapping("/my-product")
    public ResponseEntity<List<Product>> getMyProducts(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            List<Product> products = userService.getMyProducts(userName);

            if (products.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            for (Product product : products) {
                // เพิ่ม URL ของภาพ
                String imageUrl = "/images/" + product.getProductId() + ".jpg";
                product.setImageUrl(imageUrl);
                product.setImage(null); // ลบข้อมูล byte[] ออกจาก JSON Response
            }

            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}