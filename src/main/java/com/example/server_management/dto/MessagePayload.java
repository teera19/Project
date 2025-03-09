package com.example.server_management.dto;

import java.time.Instant;

public class MessagePayload {
    private int chatId;
    private int messageId;
    private String sender;
    private String message;
    private Instant timestamp;

    public MessagePayload(int chatId, int messageId, String sender, String message, Instant timestamp) {
        this.chatId = chatId;
        this.messageId = messageId;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    // ✅ Getter & Setter (Spring ต้องใช้เพื่อแปลง JSON อัตโนมัติ)
    public int getChatId() { return chatId; }
    public void setChatId(int chatId) { this.chatId = chatId; }

    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
