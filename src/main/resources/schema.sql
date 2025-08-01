CREATE TABLE IF NOT EXISTS posts (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL,
                                     text TEXT,
                                     image_path VARCHAR(255),
                                     likes_count INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tags (
                                    post_id INT,
                                    tag VARCHAR(255),
                                    FOREIGN KEY (post_id) REFERENCES posts(id)
);
CREATE TABLE IF NOT EXISTS comments (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        post_id INT,
                                        text TEXT NOT NULL,
                                        FOREIGN KEY (post_id) REFERENCES posts(id)
);
