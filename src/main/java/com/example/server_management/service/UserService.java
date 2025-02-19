package com.example.server_management.service;

import com.example.server_management.dto.ResponseProduct;
import com.example.server_management.models.*;
import com.example.server_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private ClothingDetailsRepository  clothingDetailsRepository;
    @Autowired
    private PhoneDetailsRepository phoneDetailsRepository;
    @Autowired
    private ShoesDetailsRepository shoesDetailsRepository;

    public User registerServiceMethod(User user) {
        System.out.println("Registering user: " + user.getUserName() + ", Email: " + user.getEmail());
        return userRepository.save(user);
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
    public ResponseProduct addProductToShop(String shopTitle, String name, String description, double price,
                                            MultipartFile image, int categoryId, Map<String, String> details) throws IOException {
        // ✅ ค้นหาหมวดหมู่
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));

        // ✅ ค้นหาร้านค้า
        MyShop shop = myShopRepository.findByTitle(shopTitle);
        if (shop == null) {
            throw new IllegalArgumentException("Shop not found with title: " + shopTitle);
        }

        // ✅ สร้างสินค้าใหม่
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setShop(shop);
        product.setCategory(category);

        // ✅ บันทึกสินค้าในฐานข้อมูลก่อน เพื่อให้ได้ `productId`
        Product savedProduct = productRepository.save(product);
        System.out.println("✅ Saved Product ID: " + savedProduct.getProductId());

        // ✅ บันทึกภาพและอัปเดต `imageUrl`
        String imageUrl = saveImageToFile(image, savedProduct.getProductId());
        savedProduct.setImageUrl(imageUrl);

        // ✅ บันทึกสินค้าอีกรอบ พร้อม `imageUrl`
        productRepository.save(savedProduct);

        return new ResponseProduct(
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                imageUrl, // ✅ URL รูปที่ถูกต้อง
                null
        );
    }


    // ฟังก์ชันสำหรับบันทึกภาพในระบบไฟล์
    private String saveImageToFile(MultipartFile image, int productId) throws IOException {
        if (image.isEmpty()) {
            throw new IOException("❌ No image uploaded");
        }

        // ✅ ใช้ `/tmp/images/`
        File uploadDir = new File("/tmp/images/");
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                throw new IOException("❌ Failed to create directory: " + uploadDir.getAbsolutePath());
            }
        }

        String fileName = productId + ".jpg";
        File savedFile = new File(uploadDir, fileName);
        image.transferTo(savedFile);

        return "https://project-production-f4db.up.railway.app/images/" + fileName;
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
    // ค้นหา ClothingDetails โดยใช้ productId
    public ClothingDetails findClothingDetailsByProductId(int productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;
        return clothingDetailsRepository.findByProduct(product).orElse(null);
    }

    public PhoneDetails findPhoneDetailsByProductId(int productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;
        return phoneDetailsRepository.findByProduct(product).orElse(null);
    }

    public ShoesDetails findShoesDetailsByProductId(int productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;
        return shoesDetailsRepository.findByProduct(product).orElse(null);
    }



    public void saveClothingDetails(ClothingDetails clothingDetails) {
        clothingDetailsRepository.save(clothingDetails);
    }

    public void savePhoneDetails(PhoneDetails phoneDetails) {
        phoneDetailsRepository.save(phoneDetails);
    }

    public void saveShoesDetails(ShoesDetails shoesDetails) {
        shoesDetailsRepository.save(shoesDetails);
    }


    // บันทึกไฟล์รูปภาพ

    // ฟังก์ชันสำหรับบีบอัดและบันทึกภาพ
    public void saveCompressedImage(byte[] imageBytes, int productId) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                throw new IOException("❌ Cannot read image data.");
            }

            // ✅ ตรวจสอบและสร้างโฟลเดอร์ `/tmp/images/`
            File uploadDir = new File("/tmp/images/");
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new IOException("❌ Failed to create directory: " + uploadDir.getAbsolutePath());
            }

            // ✅ บันทึกเป็น PNG โดยไม่ลดขนาด
            File outputFile = new File(uploadDir, productId + ".png");
            ImageIO.write(originalImage, "png", outputFile);

            System.out.println("✅ Image saved successfully: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to save image: " + e.getMessage());
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
