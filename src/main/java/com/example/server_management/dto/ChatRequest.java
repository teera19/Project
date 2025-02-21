package com.example.server_management.dto;

public class ChatRequest {
    private String user1;
    private String user2;
    private int productId;

    // Getter & Setter
    public String getUser1() { return user1; }
    public void setUser1(String user1) { this.user1 = user1; }

    public String getUser2() { return user2; }
    public void setUser2(String user2) { this.user2 = user2; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
}