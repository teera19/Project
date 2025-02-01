package com.example.server_management.repository;

import com.example.server_management.models.CartItem;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartUser(User user);
}


