package com.example.server_management.service;

import com.example.server_management.dto.ResponseProduct;
import com.example.server_management.models.*;
import com.example.server_management.repository.*;
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

    public int registerServiceMethod(String user_name, String name, String last_name, String email,
                                     String password, String address, String tel) {
        System.out.println("Registering user: " + user_name + ", Email: " + email);
        int result = userRepository.register(user_name, name, last_name, email, password, address, tel);
        System.out.println("Result: " + result);
        return result;
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
    public ResponseProduct addProductToShop(String shopTitle, String name, String description, double price, byte[] imageBytes, int categoryId, Map<String, String> details) {
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
        product.setImage(imageBytes);
        product.setShop(shop);
        product.setCategory(category);

        // บันทึกสินค้า
        Product savedProduct = productRepository.save(product);

        Object detailObject = null;

        // บันทึกข้อมูลเฉพาะหมวดหมู่
        if (categoryId == 1) { // สำหรับเสื้อผ้า
            ClothingDetails clothingDetails = new ClothingDetails();
            clothingDetails.setProduct(savedProduct);

            // ใช้พารามิเตอร์ที่ตรงกับที่ส่งจาก Postman
            String tearLocation = details.getOrDefault("details[tear_location]", "Unknown");
            boolean hasStain = Boolean.parseBoolean(details.getOrDefault("details[has_stain]", "false"));
            int repairCount = Integer.parseInt(details.getOrDefault("details[repair_count]", "0"));

            clothingDetails.setTearLocation(tearLocation);
            clothingDetails.setHasStain(hasStain);
            clothingDetails.setRepairCount(repairCount);

            clothingDetailsRepository.save(clothingDetails);
            detailObject = clothingDetails;


    } else if (categoryId == 2) { // สำหรับโทรศัพท์
            PhoneDetails phoneDetails = new PhoneDetails();
            phoneDetails.setProduct(savedProduct);

            // ใช้พารามิเตอร์ที่ตรงกับที่ส่งจาก Postman
            boolean basicFunctionalityStatus = Boolean.parseBoolean(details.getOrDefault("details[basic_functionality_status]", "false"));
            String nonFunctionalParts = details.getOrDefault("details[nonfunctional_parts]", "Unknown");
            String batteryStatus = details.getOrDefault("details[battery_status]", "Unknown");
            int scratchCount = Integer.parseInt(details.getOrDefault("details[scratch_count]", "0"));

            phoneDetails.setBasicFunctionalityStatus(basicFunctionalityStatus);
            phoneDetails.setNonFunctionalParts(nonFunctionalParts);
            phoneDetails.setBatteryStatus(batteryStatus);
            phoneDetails.setScratchCount(scratchCount);

            phoneDetailsRepository.save(phoneDetails);
            detailObject = phoneDetails;
        } else if (categoryId == 3) { // สำหรับรองเท้า
            ShoesDetails shoesDetails = new ShoesDetails();
            shoesDetails.setProduct(savedProduct);

            // ใช้พารามิเตอร์ที่ตรงกับที่ส่งจาก Postman
            boolean hasBrandLogo = Boolean.parseBoolean(details.getOrDefault("details[hasbrand_logo]", "false"));
            int repairCount = Integer.parseInt(details.getOrDefault("details[repair_count]", "0"));
            String tearLocation = details.getOrDefault("details[tear_location]", "Unknown");

            shoesDetails.setHasBrandLogo(hasBrandLogo);
            shoesDetails.setRepairCount(repairCount);
            shoesDetails.setTearLocation(tearLocation);

            shoesDetailsRepository.save(shoesDetails);
            detailObject = shoesDetails;
        }
        saveImageToFile(imageBytes, savedProduct.getProductId());

        // กรณีหมวดหมู่ "อื่นๆ" (categoryId == 4) ไม่มีรายละเอียดเพิ่มเติม
        String imageUrl = "http://127.0.0.1:8085/images/" + savedProduct.getProductId() + ".jpg";

// ส่ง ResponseProduct กลับ
        return new ResponseProduct(
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                imageUrl, // ตั้งค่า URL รูปภาพ
                detailObject // อ็อบเจ็กต์รายละเอียดเฉพาะ (อาจเป็น null สำหรับ categoryId == 4)
        );
    }



    // ฟังก์ชันสำหรับบันทึกภาพในระบบไฟล์
    private void saveImageToFile(byte[] imageBytes, int productId) {
        try {
            File imagesFolder = new File("images");
            if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
                throw new IOException("Failed to create images directory");
            }

            File outputFile = new File(imagesFolder, productId + ".jpg");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image != null) {
                ImageIO.write(image, "jpg", outputFile);
            } else {
                throw new IOException("Invalid image data");
            }
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
