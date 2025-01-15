package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class SearchProduct {
    @Autowired
    private UserService userService;
    @GetMapping("/search-products")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam("query") String query) {
        try {
            List<Product> products = userService.searchProductsByName(query);
            if (products.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            // เพิ่ม Image URL สำหรับแต่ละสินค้า
            for (Product product : products) {
                String imageUrl = "/images/" + product.getProductId() + ".jpg";
                product.setImageUrl(imageUrl);
                product.setImage(null); // Clear byte[] ก่อนส่งไป Frontend
            }
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
