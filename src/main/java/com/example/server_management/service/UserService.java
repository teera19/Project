package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.CategoryRepository;
import com.example.server_management.repository.MyshopRepository;
import com.example.server_management.repository.ProductRepository;
import com.example.server_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));

        // ค้นหาร้านค้า
        MyShop shop = myShopRepository.findByTitle(shopTitle);
        if (shop == null) {
            throw new IllegalArgumentException("Shop not found with title: " + shopTitle);
        }

        // สร้างสินค้าใหม่
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImage(imageBytes); // เก็บ byte[] ของภาพในฐานข้อมูล
        product.setShop(shop);
        product.setCategory(category);

        // ตั้งค่าชื่อหมวดหมู่ หากมีการใช้งานฟิลด์ categoryName
        if (category.getName() != null) {
            product.setCategoryName(category.getName());
        }

        // บันทึกสินค้า
        Product savedProduct = productRepository.save(product);

        // บันทึกภาพลงในระบบไฟล์
        saveImageToFile(imageBytes, savedProduct.getProductId());

        return savedProduct;
    }

    // ฟังก์ชันสำหรับบันทึกภาพในระบบไฟล์
    private void saveImageToFile(byte[] imageBytes, int productId) {
        try {
            // ตรวจสอบและสร้างโฟลเดอร์ images หากยังไม่มี
            File imagesFolder = new File("images");
            if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
                throw new IOException("Failed to create images directory");
            }

            // บันทึกภาพ
            File outputFile = new File(imagesFolder, productId + ".jpg");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            ImageIO.write(image, "jpg", outputFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image for product ID: " + productId, e);
        }
    }
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Product getProductById(int productId) {
        return productRepository.findById(productId).orElse(null);
    }
    @Transactional
    public Product editProduct(int productId, String shopTitle, String name, String description, double price, byte[] imageBytes, int categoryId) {
        // ค้นหาสินค้าจาก productId
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ค้นหา Shop
        MyShop shop = myShopRepository.findByTitle(shopTitle);
        if (shop == null) {
            throw new RuntimeException("Shop not found");
        }


        // ค้นหา Category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // อัปเดตข้อมูลสินค้า
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setShop(shop);
        product.setCategory(category);

        // บันทึกภาพหากมีการอัปเดต
        if (imageBytes != null) {
            // บีบอัดภาพหรือบันทึกลงไฟล์
            saveCompressedImage(imageBytes, product.getProductId());
        }

        return productRepository.save(product);
    }
    public Product findProductById(int productId) {
        return productRepository.findById(productId).orElse(null);
    }

    // ค้นหาหมวดหมู่โดยใช้ category_id
    public Category findCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId).orElse(null);
    }

    // บันทึกสินค้า
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    // บันทึกไฟล์รูปภาพ

    // ฟังก์ชันสำหรับบีบอัดและบันทึกภาพ
    public void saveCompressedImage(byte[] imageBytes, int productId) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            // บีบอัดภาพ
            int targetWidth = 100;
            int targetHeight = (int) (originalImage.getHeight() * (100.0 / originalImage.getWidth()));
            BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();

            // บันทึกภาพลงโฟลเดอร์
            File outputFile = new File("images/" + productId + ".jpg");
            ImageIO.write(scaledImage, "jpg", outputFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
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
    public List<String> findProductNamesByQuery(String query) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(query);
        return products.stream()
                .map(Product::getName) // ดึงเฉพาะชื่อสินค้า
                .limit(10) // จำกัดผลลัพธ์ที่ 10 รายการ
                .collect(Collectors.toList());
    }
    public List<Product> searchProductsByName(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }
    public MyShop findShopByProductId(int productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null && product.getShop() != null) {
            return myShopRepository.findById(product.getShop().getMyShopId()).orElse(null);
        }
        return null;
    }

    public void deleteProductById(int productId) {
        productRepository.deleteById(productId);
    }

}
