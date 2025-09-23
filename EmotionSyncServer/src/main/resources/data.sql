CREATE TABLE IF NOT EXISTS friends (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       requester_id BIGINT NOT NULL,
                                       receiver_id BIGINT NOT NULL,
                                       status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);