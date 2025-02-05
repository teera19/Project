-- Database name:
CREATE DATABASE db_project;

USE db_project;

GRANT ALL PRIVILEGES ON db_project.* TO 'root'@'localhost' WITH GRANT OPTION;
-- USER TABLE STRUCTURE:
CREATE TABLE users(
    user_id INT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tel VARCHAR(50) NOT NULL,
    PRIMARY KEY(user_id)
);
CREATE TABLE product (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2),
    image LONGBLOB,
    shop_id INT,
    FOREIGN KEY (shop_id) REFERENCES my_shop(myshop_id)
);
CREATE TABLE cart (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE cart_item (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES cart(cart_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);

UPDATE category SET name = 'เสื้อผ้า' WHERE category_id = 1;
UPDATE category SET name = 'โทรศัพท์' WHERE category_id = 2;
UPDATE category SET name = 'รองเท้า' WHERE category_id = 3;
UPDATE category SET name = 'อื่นๆ' WHERE category_id = 4;


CREATE TABLE clothing_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    has_stain VARCHAR(255),
    tear_location VARCHAR(255),
    repair_count INT,
    product_id INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);
CREATE TABLE phone_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    basic_functionality_status BOOLEAN,
    nonfunctional_parts VARCHAR(255),
    battery_status VARCHAR(255),
    scratch_count INT,
    product_id INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shoes_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hasbrand_logo BOOLEAN,
    repair_count INT,
    tear_location VARCHAR(255),
    product_id INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);
CREATE TABLE chat_rooms (
    chat_id INT AUTO_INCREMENT PRIMARY KEY,
    user1 VARCHAR(50) NOT NULL,  -- คนที่ 1 (ผู้ซื้อ)
    user2 VARCHAR(50) NOT NULL,  -- คนที่ 2 (เจ้าของร้าน)
    product_id INT NOT NULL,     -- 🛒 สินค้าที่กำลังคุยกันอยู่
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    chat_id INT NOT NULL,  -- เชื่อมกับห้องแชท
    sender VARCHAR(50) NOT NULL,  -- ใครส่งข้อความ
    message TEXT NOT NULL,  -- เนื้อหาข้อความ
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chat_id) REFERENCES chat_rooms(chat_id) ON DELETE CASCADE
);

ALTER TABLE auction DROP COLUMN image_url;
ALTER TABLE auction ADD COLUMN image LONGBLOB;
ALTER TABLE auction MODIFY COLUMN image LONGBLOB;



