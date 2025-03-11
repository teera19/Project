package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    public Cart getCartByUser(String userName) {
        User user = userRepository.findByUserName(userName);
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    public Cart addToCart(String userName, int productId, int quantity) {
        // ค้นหาผู้ใช้ปัจจุบัน
        User currentUser = userRepository.findByUserName(userName);

        if (currentUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        // ค้นหาสินค้า
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // ตรวจสอบว่าสินค้าไม่ได้มาจากร้านค้าของผู้ใช้เอง
        if (product.getShop().getUser().getUserName().equals(userName)) {
            throw new IllegalArgumentException("Cannot add your own shop's product to the cart");
        }

        // ค้นหารถเข็นของผู้ใช้
        Cart cart = getCartByUser(userName);

        // ค้นหารายการสินค้าในรถเข็น
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().equals(product))
                .findFirst();

        if (existingItem.isPresent()) {
            // เพิ่มจำนวนสินค้าถ้ามีอยู่แล้วในรถเข็น
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // เพิ่มสินค้าใหม่ในรถเข็น
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }


    public List<CartItem> viewCart(String userName) {
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return cartItemRepository.findByCartUser(user);
    }


    public void removeFromCart(String userName, int productId) {
        Cart cart = getCartByUser(userName);

        cart.getItems().removeIf(item -> item.getProduct().getProductId() == productId);
        cartRepository.save(cart);
    }

    public void clearCart(String userName) {
        Cart cart = getCartByUser(userName);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public int getCartItemCount(String userName) {
        Cart cart = getCartByUser(userName);
        return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    }


    public Order checkout(String userName) {
        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<Cart> optionalCart = cartRepository.findByUser(user);
        if (!optionalCart.isPresent()) {
            throw new IllegalArgumentException("Cart not found");
        }

        Cart cart = optionalCart.get();
        List<CartItem> cartItems = cart.getItems();  // ดึงรายการสินค้า

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        double totalAmount = calculateTotalAmount(cartItems); // คำนวณยอดรวมจากสินค้าในตะกร้า

        // สร้างคำสั่งซื้อ
        Order order = new Order(user, cart.getMyShop(), totalAmount, new Timestamp(System.currentTimeMillis())); // ใช้ getMyShop()

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            // ใช้ constructor ใหม่ในการสร้าง OrderItem
            OrderItem orderItem = new OrderItem(order, cartItem.getProduct(), cartItem.getQuantity());
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems); // ตั้งค่ารายการสินค้าในคำสั่งซื้อ
        orderRepository.save(order); // บันทึกคำสั่งซื้อ

        // ลบสินค้าจากตะกร้า
        cartRepository.deleteAllInBatch();

        return order;
    }


    public double calculateTotalAmount(List<CartItem> cartItems) {
        double totalAmount = 0.0;
        for (CartItem cartItem : cartItems) {
            double price = cartItem.getProduct().getPrice(); // ราคาของสินค้า
            totalAmount += price * cartItem.getQuantity(); // คำนวณยอดรวมจากจำนวนสินค้า
        }
        return totalAmount;
    }

}