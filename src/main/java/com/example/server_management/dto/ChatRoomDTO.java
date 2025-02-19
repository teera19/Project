package com.example.server_management.dto;


import com.example.server_management.models.ChatRoom;

public class ChatRoomDTO {
    private int chatId;
    private String user1;
    private String user2;
    private int productId;
    private String latestMessage;
    private String latestTimestamp;

    public ChatRoomDTO() {}

    public ChatRoomDTO(ChatRoom chatRoom, String latestMessage, String latestTimestamp) {
        this.chatId = chatRoom.getChatId();
        this.user1 = chatRoom.getUser1();
        this.user2 = chatRoom.getUser2();
        this.productId = chatRoom.getProductId();
        this.latestMessage = latestMessage;
        this.latestTimestamp = latestTimestamp;
    }

    public int getChatId() { return chatId; }
    public void setChatId(int chatId) { this.chatId = chatId; }

    public String getUser1() { return user1; }
    public void setUser1(String user1) { this.user1 = user1; }

    public String getUser2() { return user2; }
    public void setUser2(String user2) { this.user2 = user2; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getLatestMessage() { return latestMessage; }
    public void setLatestMessage(String latestMessage) { this.latestMessage = latestMessage; }

    public String getLatestTimestamp() { return latestTimestamp; }
    public void setLatestTimestamp(String latestTimestamp) { this.latestTimestamp = latestTimestamp; }
}
