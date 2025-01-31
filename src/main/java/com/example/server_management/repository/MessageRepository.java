package com.example.server_management.repository;

import com.example.server_management.models.Message;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :sender AND m.receiver = :receiver) OR " +
            "(m.sender = :receiver AND m.receiver = :sender) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findBySenderAndReceiverOrReceiverAndSender(
            @Param("sender") User sender,
            @Param("receiver") User receiver);
}

