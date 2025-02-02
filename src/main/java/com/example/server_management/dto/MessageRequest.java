package com.example.server_management.dto;

public class MessageRequest {
    private String sender;   // ผู้ส่ง
    private String receiver; // ผู้รับ
    private String content;  // ข้อความ
    private int productId;   // ✅ รหัสสินค้า (ใหม่)

    // Getters และ Setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}


