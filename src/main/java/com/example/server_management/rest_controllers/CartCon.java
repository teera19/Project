package com.example.server_management.rest_controllers;

import com.example.server_management.models.CartItem;
import com.example.server_management.models.Order;
import com.example.server_management.repository.MyshopRepository;
import com.example.server_management.repository.OrderRepository;
import com.example.server_management.service.CartService;
import com.example.server_management.service.CloudinaryService;
import com.example.server_management.service.SlipOkService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.server_management.models.MyShop;


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
    private MyshopRepository myshopRepository;

    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    SlipOkService slipOkService;

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

    @GetMapping("/checkout/payment-info/{orderId}")
    public ResponseEntity<?> getPaymentInfo(@PathVariable int orderId, HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getUserName().equals(userName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are not authorized to view this payment info"));
        }

        return ResponseEntity.ok(Map.of(
                "orderId", order.getOrderId(),
                "totalPrice", order.getTotalPrice(),
                "qrCodeUrl", order.getSlipUrl()
        ));
    }

    @PostMapping("/checkout/upload-slip/{orderId}")
    public ResponseEntity<?> uploadSlip(@PathVariable int orderId,
                                        @RequestParam("slip") MultipartFile slip,
                                        HttpSession session) {

        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        if (slip == null || slip.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No slip file uploaded"));
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        MyShop myShop = order.getMyShop();

        if (!order.getUser().getUserName().equals(userName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are not authorized to upload slip for this order"));
        }

        // ✅ เรียก API ตรวจสอบ Slip
        Map<String, Object> slipData = slipOkService.validateSlip(slip);
        System.out.println("Slip Data Response: " + slipData);

        if (slipData == null || slipData.containsKey("error")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Slip verification failed"));
        }

        Map<String, Object> receiver = (Map<String, Object>) slipData.get("data.receiver");
        String recipientName = receiver.get("displayName").toString().trim();
        String recipientPromptPayId = ((Map<String, Object>) receiver.get("proxy")).get("value").toString();

        // ✅ ถ้า MyShop ยังไม่มี PromptPayId ให้บันทึกอัตโนมัติ
        if (myShop.getPromptPayId() == null || myShop.getPromptPayId().isEmpty()) {
            myShop.setPromptPayId(recipientPromptPayId);
            myshopRepository.save(myShop);
        }

        // ✅ ตรวจสอบชื่อและ PromptPayId
        if (!recipientName.equalsIgnoreCase(myShop.getTitle())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Recipient name does not match"));
        }

        if (!recipientPromptPayId.equals(myShop.getPromptPayId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "PromptPay ID does not match"));
        }

        // ✅ อัปโหลดสลิปไป Cloudinary
        String slipUrl = cloudinaryService.uploadImage(slip);
        order.setSlipUrl(slipUrl);
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("message", "Slip uploaded and verified successfully", "slipUrl", slipUrl));
    }

}