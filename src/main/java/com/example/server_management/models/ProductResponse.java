package com.example.server_management.models;

public class ProductResponse {
    private String name;
    private String description;
    private double price;
    private String imageBase64;
    private String categoryName;  // เพิ่มตัวแปร categoryName

    // ปรับ constructor ให้รับ categoryName ด้วย
    public ProductResponse(String name, String description, double price, String imageBase64, String categoryName) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageBase64 = imageBase64;
        this.categoryName = categoryName;  // กำหนดค่าให้ categoryName
    }

    // Getter และ Setter
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

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    // เพิ่ม Getter และ Setter สำหรับ categoryName
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
