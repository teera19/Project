package com.example.server_management.service;

import com.example.server_management.models.MyShop;
import com.example.server_management.models.Product;
import com.example.server_management.models.ProductResponse;
import com.example.server_management.models.User;
import com.example.server_management.repository.MyshopRepository;
import com.example.server_management.repository.ProductRepository;
import com.example.server_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MyshopRepository myShopRepository;

    public int registerServiceMethod(String user_name, String name, String last_name, String email,
                                     String password, String address, String tel) {
        return userRepository.register(user_name, name, last_name, email, password, address, tel);
    }

    public int countByUserName(String userName) {
        return userRepository.countByUserName(userName);
    }

    public User loginUser(String user_name) {
        return userRepository.findByUserName(user_name);
    }

    public MyShop createShopForUser(String userName, String title, String detail) {
        User user = userRepository.findByUserName(userName);
        if (user != null) {
            MyShop myShop = new MyShop();
            myShop.setTitle(title);
            myShop.setDetail(detail);
            myShop.setUser(user);

            // บันทึก MyShop ผ่าน User และเชื่อมโยงกัน
            user.setMyShop(myShop);
            userRepository.save(user);

            return myShop; // คืนค่า MyShop ที่สร้างใหม่
        }
        return null; // หาก User ไม่พบ
    }


    public String userHasShop(String userName) {
        // ค้นหาผู้ใช้ในระบบ
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            return "User not found";
        }

        // ค้นหาร้านค้าของผู้ใช้
        Optional<MyShop> shop = myShopRepository.findByUser(user);
        System.out.println("Shop: " + shop); // Log เพื่อตรวจสอบ

        // ตรวจสอบว่าผู้ใช้มีร้านค้าหรือไม่
        if (shop.isPresent()) {
            return "User has a shop";
        } else {
            return "User does not have a shop";
        }
    }





    @Transactional
    public void addProductToShop(String shopTitle, String name, String description, double price, byte[] image) throws IOException {
        // ตรวจสอบว่ามี shop ที่ตรงกับ shopTitle หรือไม่
        MyShop shop = myShopRepository.findByTitle(shopTitle);
        if (shop == null) {
            throw new IllegalArgumentException("Shop not found with title: " + shopTitle);
        }

        // ตรวจสอบว่า image มีข้อมูลหรือไม่
        if (image == null || image.length == 0) {
            throw new IllegalArgumentException("Image file is empty");
        }

        // สร้าง Product และกำหนดค่าต่างๆ
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImage(image);  // ใช้ image เป็น byte[] โดยตรง
        product.setShop(shop);  // ตั้งค่าร้านค้า (shop)

        // บันทึก Product ลงฐานข้อมูล
        productRepository.save(product);
    }


    @Transactional
    public void editProduct(Integer productId, String name, String description, Double price, MultipartFile image) throws IOException {
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new IllegalArgumentException("Product not found with ID: " + productId));

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

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(product -> new ProductResponse(
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImage() != null ? Base64.getEncoder().encodeToString(product.getImage()) : null
                ))
                .collect(Collectors.toList());
    }
    @Transactional
    public List<ProductResponse> getMyProducts(String userName) {
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new IllegalArgumentException("User not found with username: " + userName);
        }

        MyShop shop = user.getMyShop();
        if (shop == null) {
            throw new IllegalArgumentException("No shop associated with this user.");
        }

        List<Product> products = productRepository.findByShop(shop);
        return products.stream()
                .map(product -> new ProductResponse(
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImage() != null ? Base64.getEncoder().encodeToString(product.getImage()) : null
                ))
                .collect(Collectors.toList());
    }

}
