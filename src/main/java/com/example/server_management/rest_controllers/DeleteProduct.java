package com.example.server_management.rest_controllers;

import com.example.server_management.models.MyShop;
import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class DeleteProduct {
    @Autowired
    private UserService userService;

    @DeleteMapping(value = "/delete/{product_id}",produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> deleteProduct(@PathVariable("product_id") int productId, HttpSession session) {
        // ดึง user_name จาก session
        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.FORBIDDEN);
        }

        try {
            // ค้นหาสินค้าจาก productId
            Product product = userService.findProductById(productId);
            if (product == null) {
                return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
            }

            // ค้นหาร้านค้าที่เกี่ยวข้องกับสินค้า
            MyShop shop = userService.findShopByProductId(productId);
            if (shop == null) {
                return new ResponseEntity<>("Shop not found", HttpStatus.NOT_FOUND);
            }

            // ตรวจสอบว่าร้านค้ามีเจ้าของหรือไม่
            if (shop.getUser() == null || shop.getUser().getUserName() == null) {
                return new ResponseEntity<>("Shop has no valid owner", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // ตรวจสอบว่า user_name ใน session เป็นเจ้าของร้านค้านี้หรือไม่
            if (!shop.getUser().getUserName().equals(userName)) {
                return new ResponseEntity<>("You are not authorized to delete this product", HttpStatus.UNAUTHORIZED);
            }

            // ลบสินค้า
            userService.deleteProductById(productId);

            return new ResponseEntity<>("Product deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}