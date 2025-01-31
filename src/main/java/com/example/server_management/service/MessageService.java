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

    public Message sendMessage(String senderUsername, String receiverUsername, String content) {
        // ตรวจสอบและค้นหาผู้ส่ง
        User sender = userRepository.findByUserName(senderUsername);
        if (sender == null) {
            throw new IllegalArgumentException("Sender not found");
        }

        // ตรวจสอบและค้นหาผู้รับ
        User receiver = userRepository.findByUserName(receiverUsername);
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver not found");
        }

        // สร้าง Message Entity
        Message message = new Message(sender, receiver, content);

        // บันทึกข้อความลงฐานข้อมูล
        return messageRepository.save(message);
    }

    public List<Message> getConversation(String user1Username, String user2Username) {
        // ตรวจสอบและค้นหาผู้ใช้
        User user1 = userRepository.findByUserName(user1Username);
        User user2 = userRepository.findByUserName(user2Username);

        if (user1 == null || user2 == null) {
            throw new IllegalArgumentException("User not found");
        }

        // ดึงประวัติการสนทนาระหว่าง user1 และ user2
        return messageRepository.findBySenderAndReceiverOrReceiverAndSender(user1, user2);
    }
}
