package com.example.server_management.models;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private List<Integer> productIds = new ArrayList<>();
    @Transient
    private List<Product> products;
    @ElementCollection
    @CollectionTable(name = "order_product_quantities", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")  // กำหนดว่า productId จะเป็นคีย์ในตารางนี้
    @Column(name = "quantity")
    private Map<Integer, Integer> productQuantities;

    // Default constructor
    public Order() {
        // No-argument constructor required by Hibernate
    }

    // Constructor that accepts parameters
    public Order(User user, MyShop shop, double totalPrice, Timestamp timestamp) {
        this.user = user;
        this.myShop = shop;
        this.amount = totalPrice;
        this.orderDate = timestamp;
    }
    public Map<Integer, Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Map<Integer, Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }
    public int getProductQuantity(int productId) {
        return productQuantities.getOrDefault(productId, 0); // คืนค่าจำนวนสินค้าหรือ 0 ถ้าไม่พบ
    }

    // Getters and Setters
    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
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

