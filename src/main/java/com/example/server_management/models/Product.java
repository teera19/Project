package com.example.server_management.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.util.Base64;

@Entity
@Table(name = "product")
@JsonPropertyOrder({"productId", "name", "description", "price", "image", "categoryName", "shop"})
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

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    private Category category;
    @ManyToOne
    @JoinColumn(name = "myshop_id", nullable = false)
    private MyShop shop;

    public Product() {
    }

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
    @JsonIgnore
    public String getImageBase64() {
        if (image != null) {
            return Base64.getEncoder().encodeToString(image);
        }
        return null;
    }
    public byte[] getImage() {
        return image;
    }

    private String categoryName;


    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public Category getCategory() {
        return category;
    }
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public void setCategory(Category category) {
        this.category = category;
    }


    public void setImage(byte[] image) {
        this.image = image;
    }

    public MyShop getShop() {
        return shop;
    }

    public void setShop(MyShop shop) {
        this.shop = shop;
    }


}


