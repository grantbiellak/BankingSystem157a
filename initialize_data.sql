USE bankdb;
INSERT INTO users (id, full_name, email, phone) VALUES
    (1, 'Alice Green', 'alice1@example.com', '555-0001'),
    (2, 'Bob Brown', 'bob2@example.com', '555-0002'),
    (3, 'Charlie Smith', 'charlie3@example.com', '555-0003'),
    (4, 'Diana White', 'diana4@example.com', '555-0004'),
    (5, 'Ethan Black', 'ethan5@example.com', '555-0005'),
    (6, 'Fiona Gray', 'fiona6@example.com', '555-0006'),
    (7, 'George King', 'george7@example.com', '555-0007'),
    (8, 'Hannah Stone', 'hannah8@example.com', '555-0008'),
    (9, 'Ian Wood', 'ian9@example.com', '555-0009'),
    (10, 'Julia Lake', 'julia10@example.com', '555-0010'),
    (11, 'Kevin Hill', 'kevin11@example.com', '555-0011'),
    (12, 'Laura Brooks', 'laura12@example.com', '555-0012'),
    (13, 'Michael Reed', 'mike13@example.com', '555-0013'),
    (14, 'Nina Foster', 'nina14@example.com', '555-0014'),
    (15, 'Oscar Perry', 'oscar15@example.com', '555-0015');

ALTER TABLE users AUTO_INCREMENT = 16;

INSERT INTO accounts (user_id, account_type, balance, interest_rate) VALUES
    (1, 'SAVINGS', 1500.00, 0.02),
    (1, 'CHECKING', 800.00, NULL),
    (2, 'SAVINGS', 2200.50, 0.025),
    (2, 'CHECKING', 1200.00, NULL),
    (3, 'SAVINGS', 3000.00, 0.03),
    (3, 'CHECKING', 950.75, NULL),
    (4, 'SAVINGS', 1800.00, 0.02),
    (4, 'CHECKING', 600.00, NULL),
    (5, 'SAVINGS', 2500.00, 0.025),
    (5, 'CHECKING', 1400.00, NULL),
    (6, 'SAVINGS', 2700.00, 0.03),
    (6, 'CHECKING', 1600.00, NULL),
    (7, 'SAVINGS', 1950.00, 0.02),
    (7, 'CHECKING', 720.00, NULL),
    (8, 'SAVINGS', 3100.00, 0.035),
    (8, 'CHECKING', 1800.00, NULL),
    (9, 'SAVINGS', 2000.00, 0.02),
    (9, 'CHECKING', 1000.00, NULL),
    (10, 'SAVINGS', 2600.00, 0.025),
    (10, 'CHECKING', 1300.00, NULL),
    (11, 'SAVINGS', 2800.00, 0.03),
    (11, 'CHECKING', 1500.00, NULL),
    (12, 'SAVINGS', 1900.00, 0.02),
    (12, 'CHECKING', 900.00, NULL),
    (13, 'SAVINGS', 2400.00, 0.025),
    (13, 'CHECKING', 1100.00, NULL),
    (14, 'SAVINGS', 2900.00, 0.03),
    (14, 'CHECKING', 1700.00, NULL),
    (15, 'SAVINGS', 2100.00, 0.02),
    (15, 'CHECKING', 950.00, NULL);
ALTER TABLE accounts AUTO_INCREMENT = 31;

INSERT INTO loan (loan_id, customer_id, amount, date, status) VALUES
    (1, 1, 5000.00, '2024-01-10 09:00:00', 'UNPAID'),
    (2, 2, 3000.00, '2024-01-15 10:30:00', 'PAID'),
    (3, 3, 4500.00, '2024-02-01 14:00:00', 'UNPAID'),
    (4, 4, 2000.00, '2024-02-10 11:15:00', 'PAID'),
    (5, 5, 3500.00, '2024-02-20 16:45:00', 'UNPAID'),
    (6, 6, 4000.00, '2024-03-01 09:20:00', 'PAID'),
    (7, 7, 1500.00, '2024-03-05 13:10:00', 'UNPAID'),
    (8, 8, 6000.00, '2024-03-18 15:30:00', 'UNPAID'),
    (9, 9, 2500.00, '2024-03-25 10:00:00', 'PAID'),
    (10, 10, 5500.00, '2024-04-01 08:45:00', 'UNPAID'),
    (11, 11, 3000.00, '2024-04-10 12:00:00', 'UNPAID'),
    (12, 12, 4200.00, '2024-04-18 17:25:00', 'PAID'),
    (13, 13, 2700.00, '2024-05-01 09:50:00', 'UNPAID'),
    (14, 14, 3200.00, '2024-05-07 14:40:00', 'PAID'),
    (15, 15, 3800.00, '2024-05-15 11:05:00', 'UNPAID');

ALTER TABLE loan AUTO_INCREMENT = 16;

INSERT INTO transfers (transfer_id, sender_id, receiver_id, amount, date, status) VALUES
    (1, 1, 2, 100.00, '2024-01-20 09:15:00', 'SUCCESS'),
    (2, 2, 3, 150.00, '2024-01-22 10:00:00', 'SUCCESS'),
    (3, 3, 1, 75.50, '2024-01-25 11:30:00', 'FAIL'),
    (4, 4, 5, 200.00, '2024-02-02 13:45:00', 'SUCCESS'),
    (5, 5, 6, 300.00, '2024-02-05 15:10:00', 'SUCCESS'),
    (6, 6, 7, 125.00, '2024-02-10 16:20:00', 'FAIL'),
    (7, 7, 8, 180.00, '2024-02-15 09:05:00', 'SUCCESS'),
    (8, 8, 9, 220.00, '2024-02-18 10:40:00', 'SUCCESS'),
    (9, 9, 10, 260.00, '2024-02-20 12:30:00', 'SUCCESS'),
    (10, 10, 11, 140.00, '2024-03-01 08:55:00', 'FAIL'),
    (11, 11, 12, 190.00, '2024-03-05 11:15:00', 'SUCCESS'),
    (12, 12, 13, 210.00, '2024-03-10 13:35:00', 'SUCCESS'),
    (13, 13, 14, 230.00, '2024-03-15 14:50:00', 'SUCCESS'),
    (14, 14, 15, 175.00, '2024-03-20 16:05:00', 'SUCCESS'),
    (15, 15, 1, 195.00, '2024-03-25 17:45:00', 'SUCCESS');

ALTER TABLE transfers AUTO_INCREMENT = 16;
