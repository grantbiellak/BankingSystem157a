DROP DATABASE IF EXISTS bankdb;
CREATE DATABASE bankdb;
USE bankdb;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(50),
    email VARCHAR(50),
    phone VARCHAR(20),
    CONSTRAINT uq_email_phone UNIQUE (email, phone)
);

CREATE TABLE accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_type ENUM('CHECKING','SAVINGS') NOT NULL,
    balance DECIMAL(16,2) NOT NULL,
    interest_rate DOUBLE NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE transfers (
    transfer_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    amount DECIMAL(16,2) NOT NULL,
    date DATETIME NOT NULL,
    status ENUM('SUCCESS', 'FAIL') NOT NULL
);

CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    amount DECIMAL(16,2) NOT NULL,
    date DATETIME NOT NULL,
    status ENUM('PAID', 'UNPAID') NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
);
