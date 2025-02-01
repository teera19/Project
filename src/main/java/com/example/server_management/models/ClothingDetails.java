package com.example.server_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "clothing_details")
public class ClothingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "has_stain")
    private Boolean hasStain;

    @Column(name = "tear_location")
    private String tearLocation;

    @Column(name = "repair_count")
    private int repairCount;

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

    public Boolean getHasStain() {
        return hasStain;
    }

    public void setHasStain(Boolean hasStain) {
        this.hasStain = hasStain;
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

