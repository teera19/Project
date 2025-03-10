package com.example.server_management.models;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // ผู้ซื้อ

    @Column(nullable = false)
    private double totalPrice; // ราคารวมทั้งหมด

    @Column(nullable = false)
    private Timestamp orderDate; // วันที่สั่งซื้อ

    @Column(nullable = false)
    private String status = "PENDING";
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    // ✅ Constructor
    public Order() {}

    public Order(User user, double totalPrice, Timestamp orderDate) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }

    // ✅ Getter & Setter
    public int getOrderId() { return orderId; }
    public User getUser() { return user; }
    public double getTotalPrice() { return totalPrice; }
    public Timestamp getOrderDate() { return orderDate; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

