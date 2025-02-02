package com.example.server_management.models;

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
    private String shopDetail;

    public ProductResponse(Product product) {
        this.id = product.getProductId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageUrl = "/images/" + product.getProductId() + ".jpg"; // URL รูปภาพ
        this.categoryName = product.getCategoryName();

        // ดึงข้อมูลจาก MyShop
        if (product.getShop() != null) {
            this.shopTitle = product.getShop().getTitle();
            this.shopDetail = product.getShop().getDetail();
        } else {
            this.shopTitle = "Unknown Shop";
            this.shopDetail = "No details available";
        }
    }
    public ProductResponse(Product product, boolean includeShop) {
        this.id = product.getProductId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageUrl = "/images/" + product.getProductId() + ".jpg"; // URL รูปภาพ
        this.categoryName = product.getCategoryName();


        if (includeShop && product.getShop() != null) {
            this.shopTitle = product.getShop().getTitle();
            this.shopDetail = product.getShop().getDetail();
        } else {
            this.shopTitle = null;
            this.shopDetail = null;
        }
    }

    // Getters และ Setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getShopTitle() {
        return shopTitle;
    }

    public String getShopDetail() {
        return shopDetail;
    }
}
