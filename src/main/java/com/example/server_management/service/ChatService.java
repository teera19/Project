package com.example.server_management.service;

import com.example.server_management.models.ChatRoom;
import com.example.server_management.models.Message;
import com.example.server_management.repository.ChatRoomRepository;
import com.example.server_management.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * หา ChatRoom ถ้ายังไม่มีให้สร้างใหม่
     */
    public ChatRoom getOrCreateChatRoom(String user1, String user2, int productId) {
        return chatRoomRepository.findByUser1AndUser2AndProductId(user1, user2, productId)
                .orElseGet(() -> {
                    ChatRoom newChat = new ChatRoom(user1, user2, productId);
                    return chatRoomRepository.save(newChat);
                });
    }

    /**
     * ดึงประวัติแชทจาก chatId (แก้ปัญหา NullPointerException)
     */
    public List<Message> getChatHistory(int chatId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("⚠️ Chat room not found for chatId: " + chatId));
        return messageRepository.findByChatRoom(chatRoom);
    }

    /**
     * ส่งข้อความไปยังแชท (ป้องกัน Error 500 และเพิ่ม Log Debugging)
     */
    public Message sendMessage(int chatId, String sender, String message) {
        System.out.println("📩 Sending message to chatId: " + chatId + " from " + sender + " -> " + message);

        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("⚠️ Chat not found for chatId: " + chatId));

        Message newMessage = new Message();
        newMessage.setChatRoom(chatRoom);
        newMessage.setSender(sender);
        newMessage.setMessage(message);

        Message savedMessage = messageRepository.save(newMessage);
        System.out.println("✅ Message saved: " + savedMessage.getMessage() + " at " + savedMessage.getCreatedAt());

        return savedMessage;
    }

    /**
     * ดึง ChatRoom จาก chatId (แก้ปัญหา NullPointerException)
     */
    public ChatRoom getChatRoomById(int chatId) {
        return chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("⚠️ ChatRoom not found for chatId: " + chatId));
    }

    /**
     * ดึงรายการแชทของผู้ใช้
     */
    public List<ChatRoom> getChatsByUser(String userName) {
        return chatRoomRepository.findByUser1OrUser2(userName, userName);
    }

    /**
     * ดึงข้อความล่าสุดของแชท
     */
    public Message getLatestMessage(int chatId) {
        ChatRoom chatRoom = getChatRoomById(chatId);
        Message latestMessage = messageRepository.findTopByChatRoomOrderByTimestampDesc(chatRoom);

        if (latestMessage == null) {
            System.out.println("⚠️ No messages found for chatId: " + chatId);
            return null;
        }

        return latestMessage;
    }
}
