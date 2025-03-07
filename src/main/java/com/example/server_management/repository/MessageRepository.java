package com.example.server_management.repository;

import com.example.server_management.models.ChatRoom;
import com.example.server_management.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChatRoom(ChatRoom chatRoom);
    Optional<Message> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.chatRoom = :chatRoom " +
            "AND m.sender <> :receiver " +
            "AND m.isRead = false")
    int countUnreadMessages(@Param("chatRoom") ChatRoom chatRoom, @Param("receiver") String receiver);
}