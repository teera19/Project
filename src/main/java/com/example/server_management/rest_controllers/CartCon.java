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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.sql.Timestamp;
import java.util.*;

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

    @PostMapping(value = "/addtocart/{product_id}",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> addToCart(@PathVariable("product_id") int productId,
                                       @RequestParam("quantity") int quantity,
                                       HttpSession session) {
        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        if (quantity <= 0) { // ตรวจสอบว่า quantity มากกว่าศูนย์
            return new ResponseEntity<>("Quantity must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        try {
            // ดึงข้อมูลสินค้าเพื่อเพิ่มลงในตะกร้า
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            // ดึง myshop_id จากสินค้า
            MyShop myShop = product.getShop(); // รับข้อมูลร้านค้าจากสินค้า
            int myShopId = myShop.getMyShopId(); // ดึง myshop_id

            // ทำการเพิ่มสินค้าไปยังตะกร้า
            cartService.addToCart(userName, productId, quantity, myShopId);

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




    @GetMapping(value = "/view",produces = "application/json;charset=UTF-8")
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


    @DeleteMapping(value = "/remove",produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> removeFromCart(@RequestParam("product_id") int productId, HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        cartService.removeFromCart(userName, productId);
        return new ResponseEntity<>("Product removed from cart", HttpStatus.OK);
    }

    @DeleteMapping(value = "/clear",produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> clearCart(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        cartService.clearCart(userName);
        return new ResponseEntity<>("Cart cleared", HttpStatus.OK);
    }

    @PostMapping(value = "/checkout",produces = "application/json;charset=UTF-8")
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

            // เคลียร์ตะกร้าหลังการเช็คเอาท์ (หากต้องการ)
            // cartService.clearCart(userName); // สามารถเลือกไม่ลบตะกร้าหลังจากเช็คเอาท์

            return ResponseEntity.ok(Map.of("message", "Checkout successful", "orderId", order.getOrderId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error", "error", e.getMessage()));
        }
    }



    @GetMapping(value = "/checkout/payment-info/{orderId}",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getPaymentInfo(@PathVariable("orderId") int orderId) {
        try {
            // ดึงข้อมูลคำสั่งซื้อจาก orderId
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            // ดึงข้อมูล MyShop ที่เกี่ยวข้องกับคำสั่งซื้อ
            MyShop shop = order.getMyShop();
            if (shop == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Shop not found"));
            }

            // ส่งคืนข้อมูลธนาคารของร้านค้า
            return ResponseEntity.ok(Map.of(
                    "bankAccountNumber", shop.getBankAccountNumber(),
                    "displayName", shop.getDisplayName(),
                    "bankName", shop.getBankName(),
                    "qrCodeUrl", shop.getQrCodeUrl(),
                    "amount",order.getAmount()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error", "error", e.getMessage()));
        }
    }
    @GetMapping(value = "/checkout/payment-info-from-product/{orderId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getPaymentInfoFromProduct(@PathVariable("orderId") int orderId) {
        try {
            // ดึงข้อมูลคำสั่งซื้อจาก orderId
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            // ดึงข้อมูล MyShop ที่เกี่ยวข้องกับคำสั่งซื้อ
            MyShop shop = order.getMyShop();
            if (shop == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Shop not found"));
            }

            // ส่งคืนข้อมูลธนาคารของร้านค้า
            return ResponseEntity.ok(Map.of(
                    "bankAccountNumber", shop.getBankAccountNumber(),
                    "displayName", shop.getDisplayName(),
                    "bankName", shop.getBankName(),
                    "qrCodeUrl", shop.getQrCodeUrl(),
                    "amount", order.getAmount()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error", "error", e.getMessage()));
        }
    }

    @PostMapping(value = "/checkout/upload-slip/{orderId}",produces = "application/json;charset=UTF-8")
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
    @GetMapping(value = "/orders",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getPaidOrders(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        try {
            // ดึงรายการคำสั่งซื้อที่มีสถานะเป็น PAID สำหรับผู้ใช้
            List<Order> paidOrders = orderRepository.findByUser_UserNameAndStatus(userName, "PAID");

            if (paidOrders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(Map.of("message", "No paid orders found"));
            }

            // สร้างรายการเพื่อเก็บข้อมูลคำสั่งซื้อพร้อมรายละเอียดสินค้า
            List<Map<String, Object>> orderDetails = new ArrayList<>();
            for (Order order : paidOrders) {
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("orderId", order.getOrderId());
                orderInfo.put("totalAmount", order.getAmount());
                orderInfo.put("orderDate", order.getOrderDate());
                orderInfo.put("status", order.getStatus());
                orderInfo.put("slipUrl", order.getSlipUrl());

                // ดึงรายละเอียดสินค้า
                List<Map<String, Object>> products = new ArrayList<>();
                for (Integer productId : order.getProductIds()) {
                    Product product = productRepository.findById(productId).orElse(null);
                    if (product != null) {
                        Map<String, Object> productInfo = new HashMap<>();
                        productInfo.put("productId", product.getProductId());
                        productInfo.put("productName", product.getName());;
                        productInfo.put("price", product.getPrice());
                        productInfo.put("imageUrl", product.getImageUrl());
                        products.add(productInfo);
                    }
                }
                orderInfo.put("products", products);

                orderDetails.add(orderInfo);
            }

            return ResponseEntity.ok(orderDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error", "error", e.getMessage()));
        }
    }
    @PostMapping(value = "/buy/{productId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> buyProduct(@PathVariable("productId") int productId,
                                        @RequestParam("quantity") int quantity,
                                        HttpSession session) {
        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not logged in"));
        }

        if (quantity <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Quantity must be greater than zero"));
        }

        try {
            // ดำเนินการเช็คเอาท์จากการซื้อสินค้าทันที
            Order order = cartService.checkoutFromProduct(userName, productId, quantity);

            return ResponseEntity.ok(Map.of(
                    "message", "Order created successfully",
                    "orderId", order.getOrderId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error", "error", e.getMessage()));
        }
    }

}