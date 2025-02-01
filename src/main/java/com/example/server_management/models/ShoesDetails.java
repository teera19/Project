package com.example.server_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "shoes_details")
public class ShoesDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "hasbrand_logo")
    private boolean hasBrandLogo;

    @Column(name = "repair_count")
    private int repairCount;

    @Column(name = "tear_location")
    private String tearLocation;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isHasBrandLogo() {
        return hasBrandLogo;
    }

    public void setHasBrandLogo(boolean hasBrandLogo) {
        this.hasBrandLogo = hasBrandLogo;
    }

    public int getRepairCount() {
        return repairCount;
    }

    public void setRepairCount(int repairCount) {
        this.repairCount = repairCount;
    }

    public String getTearLocation() {
        return tearLocation;
    }

    public void setTearLocation(String tearLocation) {
        this.tearLocation = tearLocation;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}

