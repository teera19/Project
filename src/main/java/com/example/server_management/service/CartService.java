package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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

    public void addToCart(String userName, int productId, int quantity) {
        // ค้นหาผู้ใช้จากชื่อผู้ใช้
        User user = userRepository.findUserByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ค้นหาตะกร้าของผู้ใช้
        Cart cart = cartRepository.findByUser_UserName(userName);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
        }

        // ค้นหาสินค้า
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // สร้าง CartItem ใหม่
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setCart(cart);

        // เพิ่ม CartItem ไปที่ตะกร้า
        cart.getItems().add(cartItem);

        // บันทึก Cart และ CartItem
        cartRepository.save(cart);
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


    @Transactional
    public Order checkout(String userName) {
        User user = userRepository.findByUserName(userName);
        if (user == null) throw new IllegalArgumentException("User not found");

        List<CartItem> cartItems = cartItemRepository.findByCartUser(user);
        if (cartItems.isEmpty()) throw new IllegalArgumentException("Cart is empty");

        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // ✅ ดึง QR Code จากร้านค้า
        MyShop shop = cartItems.get(0).getProduct().getShop();
        if (shop == null) {
            throw new IllegalArgumentException("Shop not found");
        }

        // ✅ สร้าง Order พร้อมกับ MyShop
        Order order = new Order(user, shop, totalPrice, new Timestamp(System.currentTimeMillis()));

        // ✅ บันทึก QR Code ลงใน slipUrl
        order.setSlipUrl(shop.getQrCodeUrl());

        return orderRepository.save(order);
    }
    public List<CartItem> getCartItemsForUser(String userName) {
        // สมมติว่าเราค้นหากระเป๋าสินค้าจากฐานข้อมูลโดยการเชื่อมโยงกับผู้ใช้
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // ค้นหาข้อมูล CartItem โดยใช้ userId
        return cartItemRepository.findByCart_User_UserId(user.getUserId());
    }

}