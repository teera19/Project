package com.example.server_management.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.util.Base64;

@Entity
@Table(name = "product")
@JsonPropertyOrder({"productId", "name", "description", "price", "imageUrl", "categoryName", "shop"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int productId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private double price;

    @Lob
    @Column(name = "image")
    private byte[] image;

    @Transient
    private String imageUrl; // ใช้สำหรับส่ง URL ไปยัง Frontend

    @Transient
    private String categoryName; // ใช้สำหรับส่งชื่อหมวดหมู่สินค้า

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    private Category category;

    @ManyToOne
    @JoinColumn(name = "myshop_id", nullable = false)
    private MyShop shop;
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ClothingDetails clothingDetails;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private ShoesDetails shoesDetails;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private PhoneDetails phoneDetails;


    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryName() {
        // ดึงชื่อหมวดหมู่จาก category
        return category != null ? category.getName() : null;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public MyShop getShop() {
        return shop;
    }

    public void setShop(MyShop shop) {
        this.shop = shop;
    }
    public ShoesDetails getShoesDetails() {
        return shoesDetails;
    }

    public void setShoesDetails(ShoesDetails shoesDetails) {
        this.shoesDetails = shoesDetails;
    }

    public PhoneDetails getPhoneDetails() {
        return phoneDetails;
    }

    public void setPhoneDetails(PhoneDetails phoneDetails) {
        this.phoneDetails = phoneDetails;
    }

    public ClothingDetails getClothingDetails() {
        return clothingDetails;
    }

    public void setClothingDetails(ClothingDetails clothingDetails) {
        this.clothingDetails = clothingDetails;
    }

}



