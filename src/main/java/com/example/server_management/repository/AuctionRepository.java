package com.example.server_management.repository;

import com.example.server_management.models.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Integer> {
    List<Auction> findByOrderByAuctionIdDesc();
}

