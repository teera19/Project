package com.example.server_management.repository;

import com.example.server_management.models.MyAuction;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyAuctionRepository extends JpaRepository<MyAuction, Integer> {
    List<MyAuction> findByWinner(User winner);
}
