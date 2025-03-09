package com.example.server_management.dto;

import com.example.server_management.models.Message;

import java.sql.Timestamp;

public class MessageDTO {
    private int messageId;
    private int chatId;
    private String sender;
    private String message;
    private Timestamp createdAt;
    private boolean read;

    public MessageDTO(Message message) {
        this.messageId = message.getMessageId();
        this.chatId = message.getChatRoom().getChatId();
        this.sender = message.getSender();
        this.message = message.getMessage();
        this.createdAt = message.getCreatedAt();
        this.read = message.isRead();
    }

    // Getters and Setters...
}

