package com.example.server_management.models;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
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

    @ManyToOne
    @JoinColumn(name = "myshop_id", nullable = false) // ✅ เพิ่มความสัมพันธ์กับ MyShop
    private MyShop myShop;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private Timestamp orderDate;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column
    private String slipUrl;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // เพิ่ม fetch = FetchType.EAGER
    private List<OrderItem> orderItems;

    // ✅ Constructor
    public Order() {}

    public Order(User user, MyShop myShop, double amount, Timestamp orderDate) {
        this.user = user;
        this.myShop = myShop;
        this.amount = amount;
        this.orderDate = orderDate;
    }

    // ✅ Getter & Setter
    public int getOrderId() { return orderId; }

    public User getUser() { return user; }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getOrderDate() { return orderDate; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getSlipUrl() { return slipUrl; }

    public void setSlipUrl(String slipUrl) { this.slipUrl = slipUrl; }

    public MyShop getMyShop() { return myShop; }

    public void setMyShop(MyShop myShop) { this.myShop = myShop; }

    public List<OrderItem> getOrderItems() { return orderItems; }

    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}
