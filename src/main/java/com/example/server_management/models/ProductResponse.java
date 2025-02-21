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
    private String ownerUserName;
    private int ownerUserId;

    public ProductResponse(Product product) {
        this.id = product.getProductId();
        this.name = product.getName();  //  ดึงชื่อสินค้า
        this.description = product.getDescription();  //  ดึงคำอธิบายสินค้า
        this.price = product.getPrice();  //  ดึงราคาสินค้า
        this.imageUrl = "/images/" + product.getProductId() + ".jpg";  //  URL รูปภาพ
        this.categoryName = product.getCategoryName();  //  ดึงชื่อหมวดหมู่สินค้า

        if (product.getShop() != null) {
            this.shopTitle = product.getShop().getTitle();
            this.shopDetail = product.getShop().getDetail();

            if (product.getShop().getUser() != null) {
                this.ownerUserName = product.getShop().getUser().getUserName();
                this.ownerUserId = product.getShop().getUser().getUserId();
            }
        }
    }

    // Getter และ Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getShopTitle() {
        return shopTitle;
    }

    public void setShopTitle(String shopTitle) {
        this.shopTitle = shopTitle;
    }

    public String getShopDetail() {
        return shopDetail;
    }

    public void setShopDetail(String shopDetail) {
        this.shopDetail = shopDetail;
    }

    public String getOwnerUserName() {
        return ownerUserName;
    }

    public void setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}

