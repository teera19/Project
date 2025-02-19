package com.example.server_management.repository;

import com.example.server_management.models.ChatRoom;
import com.example.server_management.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChatRoom(ChatRoom chatRoom);
    Message findTopByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);
}
