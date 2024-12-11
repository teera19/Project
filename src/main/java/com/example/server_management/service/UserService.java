package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.CategoryRepository;
import com.example.server_management.repository.MyshopRepository;
import com.example.server_management.repository.ProductRepository;
import com.example.server_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
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
    public Product addProductToShop(String shopTitle, String name, String description, double price, byte[] imageBytes, int categoryId) {
        // ค้นหาหมวดหมู่จาก categoryId
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // ค้นหาร้านค้า
        MyShop shop = myShopRepository.findByTitle(shopTitle);

        // สร้างสินค้าใหม่
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImage(imageBytes);
        product.setShop(shop);
        product.setCategory(category);  // ตั้งค่าหมวดหมู่ให้กับสินค้า
        product.setCategoryName(category.getName());

        return productRepository.save(product);
    }
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    @Transactional
    public Product editProduct(int productId, String shopTitle, String name, String description, double price, byte[] imageBytes, int categoryId) {
        // ค้นหาสินค้าจาก productId
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // ตรวจสอบว่าผู้ใช้เป็นเจ้าของร้านค้าที่สินค้าถูกเพิ่มไว้หรือไม่
        if (!existingProduct.getShop().getTitle().equals(shopTitle)) {
            throw new IllegalArgumentException("You are not the owner of this product");
        }

        // ค้นหาหมวดหมู่จาก categoryId
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // อัพเดตข้อมูลสินค้า
        existingProduct.setName(name);
        existingProduct.setDescription(description);
        existingProduct.setPrice(price);
        existingProduct.setImage(imageBytes);
        existingProduct.setCategory(category);

        // บันทึกการเปลี่ยนแปลง
        return productRepository.save(existingProduct);
    }


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    @Transactional
    public List<Product> getMyProducts(String userName) {
        // ค้นหา User ตามชื่อ
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new IllegalArgumentException("User not found with username: " + userName);
        }

        // ค้นหา MyShop ที่เชื่อมโยงกับ User
        MyShop shop = user.getMyShop();
        if (shop == null) {
            throw new IllegalArgumentException("No shop associated with this user.");
        }

        // ค้นหา Products ที่เชื่อมโยงกับ MyShop
        List<Product> products = productRepository.findByShop(shop);
        return products;
    }


}
