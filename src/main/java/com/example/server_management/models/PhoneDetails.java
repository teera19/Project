package com.example.server_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "phone_details")
public class PhoneDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "basic_functionality_status")
    private boolean basicFunctionalityStatus; // การทำงานพื้นฐานทำงานได้ไหม

    @Column(name = "nonfunctional_parts")
    private String nonFunctionalParts; // อะไรที่ใช้ไม่ได้

    @Column(name = "battery_status")
    private String batteryStatus; // สถานะแบตเตอรี่

    @Column(name = "scratch_count")
    private int scratchCount; // จำนวนรอยขีดข่วน

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

    public boolean isBasicFunctionalityStatus() {
        return basicFunctionalityStatus;
    }

    public void setBasicFunctionalityStatus(boolean basicFunctionalityStatus) {
        this.basicFunctionalityStatus = basicFunctionalityStatus;
    }

    public String getNonFunctionalParts() {
        return nonFunctionalParts;
    }

    public void setNonFunctionalParts(String nonFunctionalParts) {
        this.nonFunctionalParts = nonFunctionalParts;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public int getScratchCount() {
        return scratchCount;
    }

    public void setScratchCount(int scratchCount) {
        this.scratchCount = scratchCount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}

