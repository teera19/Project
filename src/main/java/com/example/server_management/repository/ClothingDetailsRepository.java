package com.example.server_management.repository;

import com.example.server_management.models.ClothingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothingDetailsRepository extends JpaRepository<ClothingDetails, Integer> {
}

