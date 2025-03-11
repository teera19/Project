package com.example.server_management.repository;

import com.example.server_management.models.Cart;
import com.example.server_management.models.CartItem;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    void deleteAllInBatch();
}

