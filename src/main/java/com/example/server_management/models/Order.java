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

    @ManyToOne
    @JoinColumn(name = "myshop_id", nullable = false)
    private MyShop myShop;

    @Column(nullable = false)
    private double totalPrice;

    @Column(nullable = false)
    private Timestamp orderDate;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column
    private String slipUrl;

    // Getter & Setter
    public int getOrderId() { return orderId; }
    public User getUser() { return user; }
    public MyShop getMyShop() { return myShop; }
    public double getTotalPrice() { return totalPrice; }
    public String getSlipUrl() { return slipUrl; }
    public void setSlipUrl(String slipUrl) { this.slipUrl = slipUrl; }
}
