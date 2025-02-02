package com.example.server_management.dto;

import com.example.server_management.models.Message;

import java.util.Date;

public class MessageRespons {
    private Long id;
    private String senderUserName;
    private String receiverUserName;
    private String content;
    private Integer productId;  // เปลี่ยนเป็น Integer เพื่อรองรับค่า null
    private Date timestamp;

    public MessageRespons(Message message) {
        this.id = message.getId();
        this.senderUserName = message.getSender() != null ? message.getSender().getUserName() : "Unknown Sender";
        this.receiverUserName = message.getReceiver() != null ? message.getReceiver().getUserName() : "Unknown Receiver";
        this.content = message.getContent();
        this.productId = message.getProductId();  // รองรับค่า null
        this.timestamp = message.getTimestamp();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getSenderUserName() {
        return senderUserName;
    }

    public String getReceiverUserName() {
        return receiverUserName;
    }

    public String getContent() {
        return content;
    }

    public Integer getProductId() {
        return productId;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

