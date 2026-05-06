-- ============================================================
-- Event Registration and Attendee Management System
-- Database Setup Script
-- ============================================================

CREATE DATABASE IF NOT EXISTS event_management_db;
USE event_management_db;

-- ============================================================
-- Table: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Table: events
-- ============================================================
CREATE TABLE IF NOT EXISTS events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    event_name VARCHAR(100) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    venue VARCHAR(150) NOT NULL,
    max_slots INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Table: registrations
-- ============================================================
CREATE TABLE IF NOT EXISTS registrations (
    registration_id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    contact_number VARCHAR(11) NOT NULL,
    attendance_status ENUM('Pending','Present','Absent') NOT NULL DEFAULT 'Pending',
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    UNIQUE KEY unique_event_email (event_id, email)
);

-- ============================================================
-- Default Admin Account
-- password = "admin123" hashed with SHA-256
-- SHA-256 of "admin123" = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a
-- ============================================================
INSERT INTO users (username, password, full_name)
VALUES (
    'roxel',
    'cb8c93b371ea5db295e290425768ff8beb62239bd1cf416152ef91c104f9d45c',
    'Roxel'
)
INSERT INTO users (username, password, full_name)
VALUES (
    'lycah',
    'cd4a852c82423fc7ea71ee1f03f4936784a83a476a0c560c94bae8e5d0fbe078',
    'Lycah'
)
    INSERT INTO users (username, password, full_name)
VALUES (
    'iverson',
    'd5c89bdeefe980a57eedb2a34a4ef4ed646b6a0002ab11e711b282bb498768cf',
    'Iverson'
)
ON DUPLICATE KEY UPDATE username = username;
