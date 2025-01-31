package com.example.server_management.dto;

public class MessageRequest {
    private String sender;   // username ของผู้ส่ง
    private String receiver; // username ของผู้รับ
    private String content;  // เนื้อหาข้อความ

    // Getter และ Setter
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

