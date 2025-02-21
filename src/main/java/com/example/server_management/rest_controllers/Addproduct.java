package com.example.server_management.rest_controllers;

import com.example.server_management.dto.ResponseProduct;
import com.example.server_management.models.Category;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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
                                             @RequestParam("category_name") String categoryName, // 🛠 รับเป็นชื่อแทน
                                             @RequestParam Map<String, String> details, HttpSession session) throws IOException {
        try {
            System.out.println(" category_name = " + categoryName);

            String userName = (String) session.getAttribute("user_name");
            if (userName == null) {
                return new ResponseEntity<>("User not logged in", HttpStatus.FORBIDDEN);
            }
            //  หา category_id ตามชื่อ
            Category category = userService.findCategoryByName(categoryName);
            if (category == null) {
                return new ResponseEntity<>("Invalid category name: " + categoryName, HttpStatus.BAD_REQUEST);
            }
            int categoryId = category.getCategoryId();
            System.out.println(" Found category_id: " + categoryId);

            ResponseProduct responseProduct = userService.addProductToShop(
                    shopTitle, name, description, price, image, categoryId, details);

            return new ResponseEntity<>(responseProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}