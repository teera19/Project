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
    @JoinColumn(name = "myshop_id", nullable = false)
    private MyShop myShop;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private Timestamp orderDate;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column
    private String slipUrl;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "product_id")
    private List<Integer> productIds = new ArrayList<>();  // เก็บ product_id จากตะกร้า

    // Constructor ที่รับพารามิเตอร์
    public Order(User user, MyShop shop, double totalPrice, Timestamp timestamp) {
        this.user = user;
        this.myShop = shop;
        this.amount = totalPrice;
        this.orderDate = timestamp;
    }

    // Getter และ Setter สำหรับ orderItems และอื่นๆ
    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSlipUrl() {
        return slipUrl;
    }

    public void setSlipUrl(String slipUrl) {
        this.slipUrl = slipUrl;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MyShop getMyShop() {
        return myShop;
    }

    public void setMyShop(MyShop myShop) {
        this.myShop = myShop;
    }
}