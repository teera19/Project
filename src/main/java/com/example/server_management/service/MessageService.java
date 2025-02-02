package com.example.server_management.service;

import com.example.server_management.models.Message;
import com.example.server_management.models.User;
import com.example.server_management.repository.MessageRepository;
import com.example.server_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    public Message sendMessage(String senderUsername, String receiverUsername, String content, int productId) {
        // ตรวจสอบ sender
        User sender = userRepository.findByUserName(senderUsername);
        if (sender == null) {
            throw new IllegalArgumentException("Sender not found");
        }

        // ตรวจสอบ receiver
        User receiver = userRepository.findByUserName(receiverUsername);
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver not found");
        }

        // สร้างข้อความและบันทึก
        Message message = new Message(sender, receiver, content, productId);
        return messageRepository.save(message);
    }

    public List<Message> getConversation(String user1, String user2) {
        return messageRepository.findConversation(user1, user2);
    }
}
