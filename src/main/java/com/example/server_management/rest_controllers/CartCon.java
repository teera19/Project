package com.example.server_management.rest_controllers;

import com.example.server_management.models.CartItem;
import com.example.server_management.models.Order;
import com.example.server_management.repository.MyshopRepository;
import com.example.server_management.repository.OrderRepository;
import com.example.server_management.repository.UserRepository;
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
    @Autowired
    private UserRepository userRepository;

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

        MyShop myShop = order.getMyShop(); // ดึงข้อมูล MyShop เพื่อดึงข้อมูลธนาคาร
        return ResponseEntity.ok(Map.of(
                "orderId", order.getOrderId(),
                "amount", order.getAmount(),
                "qrCodeUrl", order.getSlipUrl(),
                "bankAccountNumber", myShop.getBankAccountNumber(),
                "bankName", myShop.getBankName(),
                "displayName", myShop.getDisplayName()
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

        // เรียก API เพื่อตรวจสอบสลิป
        try {
            // เรียก API เพื่อตรวจสอบสลิป
            Map<String, Object> slipData = slipOkService.validateSlip(slip);
            if (slipData == null || slipData.containsKey("error")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Slip verification failed"));
            }

        // ดึงข้อมูลจากสลิป
        Map<String, Object> data = (Map<String, Object>) slipData.get("data");
        Map<String, Object> receiver = (Map<String, Object>) data.get("receiver");

        // ดึงชื่อผู้รับจากสลิป
        String recipientName = receiver.get("displayName") != null
                ? receiver.get("displayName").toString().trim().replace("นาย", "").replace("นาง", "").replace("นางสาว", "").trim()
                : null;

        // เอาชื่อผู้รับในฐานข้อมูล
        String shopBankAccountName = myShop.getDisplayName().replace("นาย", "").replace("นาง", "").replace("นางสาว", "").trim();

        if (recipientName == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Recipient name is missing in slip data"));
        }

        // เปรียบเทียบแค่ 5-10 ตัวแรกของชื่อ
        int compareLength = Math.min(10, recipientName.length()); // กำหนดให้เปรียบเทียบ 5-10 ตัวแรก
        if (!recipientName.substring(0, compareLength)
                .equalsIgnoreCase(shopBankAccountName.substring(0, compareLength))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Recipient name does not match"));
        }
        // ดึง amount จาก JSON ที่ได้รับมา
            // ดึง amount จาก JSON ที่ได้รับมา
            Map<String, Object> dataFromSlip = (Map<String, Object>) slipData.get("data");  // เปลี่ยนชื่อเป็น dataFromSlip
            if (dataFromSlip != null) {
                Object amountObj = dataFromSlip.get("amount");  // ใช้ dataFromSlip แทน data
                if (amountObj == null || !(amountObj instanceof Number)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Amount is missing or invalid in slip data"));
                }
                double amountFromSlip = ((Number) amountObj).doubleValue();
                if (amountFromSlip != order.getAmount()) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Amount does not match"));
                }
            }


            // ✅ อัปโหลดสลิปไป Cloudinary
        String slipUrl = cloudinaryService.uploadImage(slip);
        order.setSlipUrl(slipUrl);
        order.setStatus("PAID");
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("message", "Slip uploaded and verified successfully", "slipUrl", slipUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal Server Error", "error", e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrdersByUser(HttpSession session) {
        // ดึง userId จาก session
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        // ดึง userId จากฐานข้อมูลตาม userName
        int userId = userRepository.findByUserName(userName).getUserId();  // สมมติว่า userRepository ค้นหาผู้ใช้จาก userName

        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, "PAID");

        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "No paid orders found for user."));
        }

        return ResponseEntity.ok(orders);
    }

}