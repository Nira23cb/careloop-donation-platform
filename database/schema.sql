-- CareLoop MySQL Database Schema
-- Run this script to create the database and tables

CREATE DATABASE IF NOT EXISTS careloop;
USE careloop;

-- Users table: donors, NGOs, and volunteers
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('DONOR', 'NGO', 'VOLUNTEER') NOT NULL,
    reliability_score INT DEFAULT 100,
    cancel_count INT DEFAULT 0,
    unreliable BOOLEAN DEFAULT FALSE,
    rating DECIMAL(3,2) DEFAULT 5.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Donations table
CREATE TABLE donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    type ENUM('FOOD', 'CLOTHES', 'BOOKS', 'ESSENTIALS') NOT NULL,
    quantity INT NOT NULL,
    location VARCHAR(255) NOT NULL,
    prepared_time DATETIME NULL,
    expiry_time DATETIME NULL,
    veg_type ENUM('VEG', 'NON_VEG', 'NA') DEFAULT 'NA',
    status ENUM('PENDING', 'VERIFIED', 'ASSIGNED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING',
    verified_by BIGINT NULL,
    verified_at DATETIME NULL,
    cancel_reason VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES users(id),
    FOREIGN KEY (verified_by) REFERENCES users(id)
);

-- Deliveries table: links donations to volunteers
CREATE TABLE deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donation_id BIGINT NOT NULL UNIQUE,
    volunteer_id BIGINT NOT NULL,
    status ENUM('ASSIGNED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') DEFAULT 'ASSIGNED',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at DATETIME NULL,
    FOREIGN KEY (donation_id) REFERENCES donations(id),
    FOREIGN KEY (volunteer_id) REFERENCES users(id)
);

-- Notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    read_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Sample data (optional - passwords are 'password123' hashed with BCrypt)
-- Use the app's register API instead for real data
