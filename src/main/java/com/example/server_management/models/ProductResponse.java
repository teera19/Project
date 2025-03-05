package com.example.server_management.models;

import com.example.server_management.dto.ResponseProduct;
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
    private String ownerUserName;
    private String defectDetails;  // ✅ เพิ่ม defectDetails

    // ✅ Constructor สำหรับสร้างจาก `Product`
    public ProductResponse(Product product) {
        this.id = product.getProductId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.categoryName = (product.getCategory() != null) ? product.getCategory().getName() : "Unknown"; // ✅ ตรวจสอบ `null`
        this.shopTitle = (product.getShop() != null) ? product.getShop().getTitle() : "Unknown"; // ✅ ตรวจสอบ `null`
        this.ownerUserName = (product.getShop() != null && product.getShop().getUser() != null)
                ? product.getShop().getUser().getUserName()
                : "Unknown"; // ✅ ตรวจสอบ `null`
        this.defectDetails = product.getDefectDetails(); // ✅ ดึง defectDetails จาก Product
    }

    // ✅ Constructor สำหรับสร้างจาก `ResponseProduct`
    public ProductResponse(ResponseProduct responseProduct) {
        this.id = responseProduct.getId();
        this.name = responseProduct.getName();
        this.description = responseProduct.getDescription();
        this.price = responseProduct.getPrice();
        this.imageUrl = responseProduct.getImageUrl();
        this.categoryName = responseProduct.getCategoryName();
        this.shopTitle = responseProduct.getShopTitle();
        this.ownerUserName = responseProduct.getOwnerUserName();
        this.defectDetails = responseProduct.getDefectDetails();
    }

    // ✅ Getter
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getCategoryName() { return categoryName; }
    public String getShopTitle() { return shopTitle; }
    public String getOwnerUserName() { return ownerUserName; }
    public String getDefectDetails() { return defectDetails; }  // ✅ Getter สำหรับ defectDetails
}
