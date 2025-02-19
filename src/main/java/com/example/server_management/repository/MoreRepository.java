package com.example.server_management.repository;

import com.example.server_management.models.More;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoreRepository extends JpaRepository<More, Integer> {
}