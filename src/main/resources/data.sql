CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT, 
    receiver_id INT,
    text TEXT,
    timestamp DATE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

INSERT IGNORE INTO users
(username, email, password, avatar, is_online)
VALUES
    ('Snevs', 'blabla@', '$2a$10$FO4k9CqYocbZDk5NSMTMCOlGPDRtvp8soLeX0ZeTvp6xau.TY4xzK', 'placeholder1.png', 1), --password = 1234
    ('Mås', 'test@ok.com', '$2a$10$H1uL6WqqkfAYmGuL6Bb8tuV69PBnFu6yN80.tKyxHasGzyHxvny4y', 'placeholder2.png', 0), --password = lol
    ('BjörnBoy', 'mail', '$2a$10$JyHHmOdhzBSNfDcSsNNv/uT/uEi1BU6amnBZtisEaQo4Vk6LhVFlO', 'placeholder3.png', 1); --Password = pass

INSERT IGNORE INTO messages
(conversation_id, sender_id, content, created_at)
VALUES
    (1, 2, 'Hey, what is up?', '2025-06-01'),
    (1, 1, 'Not much, just chilling.', '2025-06-01'),
    (1, 2, 'You coming to the event?', '2025-06-02'),
    (1, 1, 'Absolutely! I''ll be there.', '2025-06-02'),
    (2, 3, 'Got the files you sent. Thanks!', '2025-06-03');
