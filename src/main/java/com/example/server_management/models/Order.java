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
    private User user;

    @Column(nullable = false)
    private double totalPrice;

    @Column(nullable = false)
    private Timestamp orderDate;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column
    private String slipUrl;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    public Order() {}

    public Order(User user, double totalPrice, Timestamp orderDate) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }

    // Getter & Setter
    public int getOrderId() { return orderId; }
    public User getUser() { return user; }
    public double getTotalPrice() { return totalPrice; }
    public Timestamp getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSlipUrl() { return slipUrl; }
    public void setSlipUrl(String slipUrl) { this.slipUrl = slipUrl; }
    public List<OrderItem> getOrderItems() { return orderItems; }
}
