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

    public Cart getCartByUser(String userName) {
        User user = userRepository.findByUserName(userName);
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    public void addToCart(String userName, int productId, int quantity, int myShopId) {
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

        // ตรวจสอบว่า myShopId ตรงกับเจ้าของสินค้าหรือไม่
        if (product.getShop().getMyShopId() != myShopId) {
            throw new IllegalArgumentException("The product does not belong to the given shop.");
        }

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
    public Order checkout(String userName, Integer productId, Integer quantity) {
        // หาผู้ใช้จาก userName
        User user = userRepository.findByUserName(userName);
        if (user == null) throw new IllegalArgumentException("User not found");

        // กรณีถ้ามีสินค้าในตะกร้า
        if (productId == null || quantity == null) {
            List<CartItem> cartItems = cartItemRepository.findByCartUser(user);
            if (cartItems.isEmpty()) throw new IllegalArgumentException("Cart is empty");

            double totalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            // ดึงข้อมูลร้านค้าที่เกี่ยวข้อง
            MyShop shop = cartItems.get(0).getProduct().getShop();
            if (shop == null) {
                throw new IllegalArgumentException("Shop not found");
            }

            // สร้างคำสั่งซื้อจากสินค้าทั้งหมดในตะกร้า
            Order order = new Order(user, shop, totalPrice, new Timestamp(System.currentTimeMillis()));

            // บันทึก QR Code ลงใน slipUrl
            order.setSlipUrl(shop.getQrCodeUrl());

            // บันทึกคำสั่งซื้อ
            Order savedOrder = orderRepository.save(order);

            return savedOrder;

        } else {
            // กรณีซื้อสินค้าทันทีจาก productId และ quantity
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            // ดึงข้อมูลร้านค้าที่เกี่ยวข้องกับสินค้า
            MyShop shop = product.getShop();
            if (shop == null) {
                throw new IllegalArgumentException("Shop not found");
            }

            // คำนวณราคาสินค้า
            double totalAmount = product.getPrice() * quantity;

            // สร้างคำสั่งซื้อจากการซื้อสินค้าทันที
            Order order = new Order(user, shop, totalAmount, new Timestamp(System.currentTimeMillis()));
            order.setProductIds(Collections.singletonList(productId));  // ตั้งค่ารายการสินค้าในคำสั่งซื้อ

            // บันทึกคำสั่งซื้อ
            Order savedOrder = orderRepository.save(order);

            return savedOrder;
        }
    }
    @Transactional
    public Order checkoutFromProduct(String userName, Integer productId, Integer quantity) {
        User user = userRepository.findByUserName(userName);
        if (user == null) throw new IllegalArgumentException("User not found");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        MyShop shop = product.getShop();
        if (shop == null) {
            throw new IllegalArgumentException("Shop not found");
        }

        double totalAmount = product.getPrice() * quantity;

        Order order = new Order(user, shop, totalAmount, new Timestamp(System.currentTimeMillis()));
        order.setProductIds(Collections.singletonList(productId));  // เก็บรายการสินค้าจากการซื้อสินค้าเดี่ยว
        orderRepository.save(order);

        return order;
    }

}