package com.example.server_management.rest_controllers;

import com.example.server_management.models.*;
import com.example.server_management.repository.MyshopRepository;
import com.example.server_management.repository.OrderRepository;
import com.example.server_management.repository.ProductRepository;
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


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/")
public class CartCon {
    private static final Logger log = LoggerFactory.getLogger(CartCon.class);
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
    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/addtocart/{product_id}")
    public ResponseEntity<?> addToCart(@PathVariable("product_id") int productId,
                                       @RequestParam("quantity") int quantity,
                                       HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        try {
            cartService.addToCart(userName, productId, quantity);

            // ดึงข้อมูลสินค้าเพื่อส่งกลับ
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            return ResponseEntity.ok(Map.of(
                    "message", "Product added to cart successfully",
                    "product", product // ส่งข้อมูลสินค้า
            ));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // จับข้อผิดพลาดอื่นๆ
            return new ResponseEntity<>(Map.of("message", "Internal Server Error", "error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
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
            // ดึงข้อมูลตะกร้าสินค้า
            List<CartItem> cartItems = cartService.viewCart(userName);
            if (cartItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Your cart is empty"));
            }

            // คำนวณยอดรวมและสร้างคำสั่งซื้อใหม่
            double totalAmount = 0;
            List<Integer> productIds = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                totalAmount += cartItem.getProduct().getPrice() * cartItem.getQuantity();
                productIds.add(cartItem.getProduct().getProductId());  // เก็บ productId จาก CartItem
            }

            // ดึงข้อมูล MyShop
            MyShop shop = cartItems.get(0).getProduct().getShop();
            if (shop == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Shop not found"));
            }

            // สร้างคำสั่งซื้อใหม่
            Order order = new Order(
                    userRepository.findUserByUserName(userName).get(),
                    shop,
                    totalAmount,
                    new Timestamp(System.currentTimeMillis())
            );
            order.setProductIds(productIds);  // ตั้งค่ารายการสินค้าในคำสั่งซื้อ

            orderRepository.save(order);  // บันทึกคำสั่งซื้อใหม่ในฐานข้อมูล

            // เคลียร์ตะกร้า
            cartService.clearCart(userName);

            return ResponseEntity.ok(Map.of("message", "Checkout successful", "orderId", order.getOrderId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error", "error", e.getMessage()));
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
        log.info("Starting the upload process for orderId: " + orderId + " with user: " + userName);

        if (userName == null) {
            log.warn("User not logged in for orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        if (slip == null || slip.isEmpty()) {
            log.warn("No slip file uploaded for orderId: {}", orderId);
            return ResponseEntity.badRequest().body(Map.of("message", "No slip file uploaded"));
        }

        try {
            // Attempt to load the order and check if it exists
            log.info("Attempting to load order with ID: {}", orderId);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found with ID: {}", orderId);
                        return new IllegalArgumentException("Order not found");
                    });

            log.info("Order found for orderId: {}", orderId);

            if (!order.getUser().getUserName().equals(userName)) {
                log.warn("User is not authorized to upload slip for orderId: {}", orderId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You are not authorized to upload slip for this order"));
            }

            // ตรวจสอบข้อมูลสลิป
            log.info("Validating slip for orderId: {}", orderId);
            Map<String, Object> slipData = slipOkService.validateSlip(slip);
            if (slipData == null || slipData.containsKey("error")) {
                log.error("Slip verification failed for orderId: {}", orderId);
                return ResponseEntity.badRequest().body(Map.of("message", "Slip verification failed"));
            }

            // อัปโหลดสลิปไปที่ Cloudinary
            String slipUrl = cloudinaryService.uploadImage(slip);
            log.info("Slip uploaded to Cloudinary for orderId: {} with URL: {}", orderId, slipUrl);
            order.setSlipUrl(slipUrl);
            order.setStatus("PAID");  // เปลี่ยนสถานะคำสั่งซื้อเป็น PAID
            orderRepository.save(order);

            log.info("Order status updated to PAID for orderId: {}", orderId);
            return ResponseEntity.ok(Map.of("message", "Slip uploaded and verified successfully", "slipUrl", slipUrl));

        } catch (Exception e) {
            log.error("Error during the upload process for orderId: " + orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error", "error", e.getMessage()));
        }
    }
    @GetMapping("/orders")
    public ResponseEntity<?> getOrdersByUser(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        Optional<User> userOptional = userRepository.findUserByUserName(userName);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOptional.get(); // ดึงข้อมูลผู้ใช้
        int userId = user.getUserId();

        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, "PAID");

        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "No paid orders found for user."));
        }

        return ResponseEntity.ok(orders);
    }

}