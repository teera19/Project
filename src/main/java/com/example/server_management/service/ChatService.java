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

    public ChatRoom getOrCreateChatRoom(String user1, String user2, int productId) {
        return chatRoomRepository.findByUser1AndUser2AndProductId(user1, user2, productId)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(user1, user2, productId)));
    }


    public List<Message> getChatHistory(int chatId) {
        return messageRepository.findByChatRoom(chatRoomRepository.findById(chatId).orElse(null));
    }

    public Message sendMessage(int chatId, String sender, String message) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        Message newMessage = new Message();
        newMessage.setChatRoom(chatRoom);
        newMessage.setSender(sender);
        newMessage.setMessage(message);
        return messageRepository.save(newMessage);
    }
    public ChatRoom getChatRoomById(int chatId) {
        return chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found"));
    }
    public List<ChatRoom> getChatsByUser(String userName) {
        return chatRoomRepository.findByUser1OrUser2(userName, userName);
    }


}