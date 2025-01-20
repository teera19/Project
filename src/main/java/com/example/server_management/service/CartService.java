package com.example.server_management.service;

import com.example.server_management.models.Cart;
import com.example.server_management.models.CartItem;
import com.example.server_management.models.Product;
import com.example.server_management.models.User;
import com.example.server_management.repository.CartRepository;
import com.example.server_management.repository.ProductRepository;
import com.example.server_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Cart cart = getCartByUser(userName);
        return cart.getItems();
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
}

