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
    private String ownerUserName;
    private Object details; // ✅ เพิ่มฟิลด์ details เพื่อส่งข้อมูลหมวดหมู่สินค้า

    // ✅ Constructor แปลงจาก Product
    public ProductResponse(Product product) {
        this.id = product.getProductId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.categoryName = product.getCategory().getName();
        this.shopTitle = product.getShop().getTitle();

        // ✅ ดึง Owner ของร้านค้า
        if (product.getShop() != null && product.getShop().getUser() != null) {
            this.ownerUserName = product.getShop().getUser().getUserName();
        } else {
            this.ownerUserName = "Unknown";
        }

        // ✅ ตรวจสอบประเภทหมวดหมู่ และดึงรายละเอียดสินค้า
        switch (product.getCategory().getCategoryId()) {
            case 1: // หมวดหมู่เสื้อผ้า
                this.details = product.getClothingDetails();
                break;
            case 2: // หมวดหมู่โทรศัพท์
                this.details = product.getPhoneDetails();
                break;
            case 3: // หมวดหมู่รองเท้า
                this.details = product.getShoesDetails();
                break;
            case 4: // หมวดหมู่สินค้าทั่วไป
                this.details = product.getMore();
                break;
            default:
                this.details = null;
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
    public String getOwnerUserName() { return ownerUserName; }
    public Object getDetails() { return details; } // ✅ Getter สำหรับข้อมูลหมวดหมู่สินค้า
}
