package com.example.server_management.dto;

public class ResponseProduct {
    private int id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String categoryName;
    private String shopTitle;
    private String ownerUserName;
    private String defectDetails;  // ✅ เพิ่ม defectDetails

    // ✅ Constructor สำหรับเก็บข้อมูล
    public ResponseProduct(int id, String name, String description, double price,
                           String imageUrl, String categoryName, String shopTitle,
                           String ownerUserName, String defectDetails) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.categoryName = categoryName;
        this.shopTitle = shopTitle;
        this.ownerUserName = ownerUserName;
        this.defectDetails = defectDetails;  // ✅ กำหนดค่า defectDetails
    }

    // ✅ Getter และ Setter
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getCategoryName() { return categoryName; }
    public String getShopTitle() { return shopTitle; }
    public String getOwnerUserName() { return ownerUserName; }
    public String getDefectDetails() { return defectDetails; }
}
