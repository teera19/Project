package com.example.server_management.dto;


import com.example.server_management.models.MyAuction;

public class MyAuctionResponse {
    private int auctionId;
    private String productName;
    private String description;
    private String imageUrl;

    public MyAuctionResponse(MyAuction myAuction) {
        this.auctionId = myAuction.getAuction().getAuctionId();
        this.productName = myAuction.getAuction().getProductName();
        this.description = myAuction.getAuction().getDescription();
        this.imageUrl = myAuction.getAuction().getImageUrl() != null ?
                myAuction.getAuction().getImageUrl() : "/images/default.jpg";
    }

    // Getter
    public int getAuctionId() { return auctionId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}

