CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT, 
    reciver_id INT,
    text TEXT,
    timestamp DATE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (reciver_id) REFERENCES users(id)
);

INSERT IGNORE INTO users
(username, email, password)
VALUES
    ('Snevs', 'blabla@', '1234', 'placeholder1.png'),
    ('Mås', 'test@ok.com', 'lol', 'placeholder2.png'),
    ('BjörnBoy', 'mail', 'ginger', 'placeholder3.png');

INSERT IGNORE INTO messages
(sender_id, reciver_id, text, timestamp)
VALUES
    (1, 2, 'Hey, what''s up?', '2025-06-01'),
    (2, 1, 'Not much, just chilling.', '2025-06-01'),
    (3, 1, 'You coming to the event?', '2025-06-02'),
    (1, 3, 'Absolutely! I''ll be there.', '2025-06-02'),
    (2, 3, 'Got the files you sent. Thanks!', '2025-06-03');
