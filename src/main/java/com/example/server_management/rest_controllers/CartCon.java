package com.example.server_management.rest_controllers;

import com.example.server_management.models.CartItem;
import com.example.server_management.models.Order;
import com.example.server_management.repository.OrderRepository;
import com.example.server_management.service.CartService;
import com.example.server_management.service.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class CartCon {
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/addtocart/{product_id}")

    public ResponseEntity<String> addToCart(@PathVariable("product_id") int productId,
                                            @RequestParam("quantity") int quantity,
                                            HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        try {
            cartService.addToCart(userName, productId, quantity);
            return new ResponseEntity<>("Product added to cart successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/view")
    public ResponseEntity<List<CartItem>> viewCart(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<CartItem> cartItems = cartService.viewCart(userName);
            if (cartItems.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(cartItems, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }





    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromCart(@RequestParam("product_id") int productId, HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        cartService.removeFromCart(userName, productId);
        return new ResponseEntity<>("Product removed from cart", HttpStatus.OK);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        cartService.clearCart(userName);
        return new ResponseEntity<>("Cart cleared", HttpStatus.OK);
    }
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        try {
            Order order = cartService.checkout(userName);
            return ResponseEntity.ok(Map.of("message", "Checkout successful", "orderId", order.getOrderId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping(value = "/upload-slip/{orderId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadPaymentSlip(@PathVariable int orderId,
                                               @RequestParam("slip") MultipartFile slipImage) {

        if (slipImage.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Slip image is required."));
        }

        // ✅ ค้นหา Order จาก orderId
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        try {
            // ✅ อัปโหลดรูปสลิปขึ้น Cloudinary
            String slipUrl = cloudinaryService.uploadImage(slipImage);

            // ✅ บันทึก URL ของสลิปใน Order
            order.setSlipUrl(slipUrl);
            order.setStatus("PAID"); // อัปเดตสถานะเป็นจ่ายเงินแล้ว
            orderRepository.save(order);

            return ResponseEntity.ok(Map.of("message", "Slip uploaded successfully", "slipUrl", slipUrl));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload slip", "error", e.getMessage()));
        }
    }

}
