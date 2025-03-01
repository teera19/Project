package com.example.server_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int chatId;

    @Column(name = "user1", nullable = false)
    private String user1; // ผู้ซื้อ

    @Column(name = "user2", nullable = false)
    private String user2; // เจ้าของร้าน

    @Column(name = "product_id", nullable = false)
    private int productId; // 🛒 สินค้าที่กำลังคุยกันอยู่

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Message> messages;

    // 🛠 Constructor ว่าง
    public ChatRoom() {
    }

    // 🛠 Constructor สำหรับสร้าง ChatRoom ใหม่
    public ChatRoom(String user1, String user2, int productId) {
        this.user1 = user1;
        this.user2 = user2;
        this.productId = productId;
    }
    @Transient  // ไม่บันทึกลงฐานข้อมูล
    private Message latestMessage;

    //  Getter & Setter
    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "chatId=" + chatId +
                ", user1='" + user1 + '\'' +
                ", user2='" + user2 + '\'' +
                ", productId=" + productId +
                '}';
    }
    public Message getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(Message latestMessage) {
        this.latestMessage = latestMessage;
    }
}