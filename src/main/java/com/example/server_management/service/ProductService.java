package com.example.server_management.service;

import com.example.server_management.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public String findSellerByProductId(int productId) {
        return productRepository.findSellerByProductId(productId);
    }
}
