package com.example.server_management.models;

import com.fasterxml.jackson.annotation.JsonInclude;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private int id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String categoryName;
    private String shopTitle;
    private String ownerUserName; // ✅ เพิ่ม ownerUserName

    // ✅ Constructor แปลงจาก Product
    public ProductResponse(Product product) {
        this.id = product.getProductId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();  // ✅ ใช้ `imageUrl` แทน `image`
        this.categoryName = product.getCategory().getName();
        this.shopTitle = product.getShop().getTitle();

        // ✅ ดึง Owner ของร้านค้า
        if (product.getShop() != null && product.getShop().getUser() != null) {
            this.ownerUserName = product.getShop().getUser().getUserName();
        } else {
            this.ownerUserName = "Unknown"; // เผื่อกรณีไม่มีข้อมูล
        }
    }

    // ✅ Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getCategoryName() { return categoryName; }
    public String getShopTitle() { return shopTitle; }
    public String getOwnerUserName() { return ownerUserName; } // ✅ Getter ใหม่
}
