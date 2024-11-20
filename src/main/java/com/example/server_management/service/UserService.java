package com.example.server_management.service;

import com.example.server_management.models.MyShop;
import com.example.server_management.models.Product;
import com.example.server_management.models.User;
import com.example.server_management.repository.MyshopRepository;  // แก้ชื่อ repository
import com.example.server_management.repository.ProductRepository;
import com.example.server_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MyshopRepository myShopRepository;  // แก้ชื่อ repository

    public int registerServiceMethod(String user_name,
                                     String name,
                                     String last_name,
                                     String email,
                                     String password,
                                     String address,
                                     String tel) {
        return userRepository.register(user_name, name, last_name, email, password, address, tel);
    }

    public int countByUserName(String userName) {
        return userRepository.countByUserName(userName);
    }

    public User loginUser(String user_name) {
        return userRepository.findByUserName(user_name);
    }

    public void createShopForUser(String userName, String title, String detail) { // ลบ static ออก
        User user = userRepository.findByUserName(userName);

        if (user != null) {
            MyShop myShop = new MyShop();
            myShop.setTitle(title);
            myShop.setDetail(detail);
            myShop.setUser(user);

            user.setMyShop(myShop);
            userRepository.save(user);
        }
    }

    @Transactional
    public void addProductToShop(String shopTitle, String name, String description, double price, MultipartFile image) throws IOException {
        // หา shop ตาม shopTitle
        MyShop shop = myShopRepository.findByTitle(shopTitle);  // แก้ชื่อ repository
        if (shop == null) {
            throw new IllegalArgumentException("Shop not found with title: " + shopTitle);
        }

        // แปลง MultipartFile เป็น byte[]
        byte[] imageBytes = image.getBytes();

        // สร้าง Product
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImage(imageBytes); // บันทึกภาพเป็น byte[]
        product.setShop(shop);

        // บันทึก Product ลงฐานข้อมูล
        productRepository.save(product);
    }


    @Transactional
    public void editProduct(Long productId, String name, String description, Double price, MultipartFile image) throws IOException {
        Product product = productRepository.findById(Math.toIntExact(productId)).orElseThrow(() ->
                new IllegalArgumentException("Product not found with ID: " + productId));

        // ตรวจสอบว่าแต่ละฟิลด์ไม่เป็น null และทำการอัปเดต
        if (name != null) {
            product.setName(name);
        }
        if (description != null) {
            product.setDescription(description);
        }
        if (price != null) {
            product.setPrice(price);
        }
        if (image != null) {
            product.setImage(image.getBytes());
        }

        productRepository.save(product);
    }


}
