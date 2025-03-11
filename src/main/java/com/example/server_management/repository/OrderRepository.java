package com.example.server_management.repository;

import com.example.server_management.models.Order;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser(User user);
    List<Order> findByUserAndStatus(User user, String status);
    // เปลี่ยนจากการค้นหา "user.id" เป็น "user.userId"
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product WHERE o.user.userId = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") int userId, @Param("status") String status);


}

