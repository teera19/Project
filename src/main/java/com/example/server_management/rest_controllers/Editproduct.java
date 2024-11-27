package com.example.server_management.rest_controllers;

import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class Editproduct {
    @Autowired
    private UserService userService;
    @PostMapping("/edit-product")
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
}
