package com.example.server_management.repository;

import com.example.server_management.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    Optional<ChatRoom> findByUser1AndUser2AndProductId(String user1, String user2, int productId);
    List<ChatRoom> findByUser1OrUser2(String user1, String user2);
}