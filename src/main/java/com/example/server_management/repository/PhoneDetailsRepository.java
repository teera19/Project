package com.example.server_management.repository;

import com.example.server_management.models.PhoneDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneDetailsRepository extends JpaRepository<PhoneDetails, Integer> {
}
