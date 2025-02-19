package com.example.server_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "shoes_details")
public class ShoesDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "hasbrand_logo")
    private String hasBrandLogo; // ✅ เปลี่ยนจาก `boolean` เป็น `String`

    @Column(name = "tear_location")
    private String tearLocation;

    @Column(name = "repair_count")
    private int repairCount;

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

    public String getHasBrandLogo() {
        return hasBrandLogo;
    }

    public void setHasBrandLogo(String hasBrandLogo) {
        this.hasBrandLogo = hasBrandLogo;
    }

    public String getTearLocation() {
        return tearLocation;
    }

    public void setTearLocation(String tearLocation) {
        this.tearLocation = tearLocation;
    }

    public int getRepairCount() {
        return repairCount;
    }

    public void setRepairCount(int repairCount) {
        this.repairCount = repairCount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
