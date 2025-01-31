package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/")
public class Allproduct {

    @Autowired
    private UserService userService;

    // API สำหรับดึงรายการสินค้าทั้งหมด
    @GetMapping("/all-product")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = userService.getAllProducts();

            if (products.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            for (Product product : products) {
                String imageUrl = "/images/" + product.getProductId() + ".jpg";
                product.setImageUrl(imageUrl);
                product.setImage(null); // เคลียร์ byte[] ก่อนส่งไปยัง Frontend
            }

            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API สำหรับดึงรายละเอียดสินค้า
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") int productId) {
        try {
            Product product = userService.getProductById(productId);

            if (product == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // เพิ่ม URL ของรูปภาพ และเคลียร์ข้อมูล byte[] ก่อนส่งไป Front-end
            String imageUrl = "/images/" + product.getProductId() + ".jpg";
            product.setImageUrl(imageUrl);
            product.setImage(null); // เคลียร์ byte[] ก่อนส่งไปยัง Frontend

            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


