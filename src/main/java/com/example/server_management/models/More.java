package com.example.server_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "more")
public class More {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "flawed_point")
    private String flawedPoint;

    // ✅ เพิ่มความสัมพันธ์กับ `Product`
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ✅ Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFlawedPoint() {
        return flawedPoint;
    }

    public void setFlawedPoint(String flawedPoint) {
        this.flawedPoint = flawedPoint;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}


