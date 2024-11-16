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
