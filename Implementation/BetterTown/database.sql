CREATE DATABASE IF NOT EXISTS BetterTown;
USE BetterTown;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'user' -- 'user' or 'admin'
);

-- Issues table
CREATE TABLE IF NOT EXISTS issue (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_data LONGBLOB,
    priority INT DEFAULT 0,
    city VARCHAR(255),
    address VARCHAR(255),
    date DATETIME,
    username VARCHAR(255),
    status VARCHAR(50) DEFAULT 'Open',
    latitude DOUBLE,
    longitude DOUBLE
);

-- Comments table
CREATE TABLE IF NOT EXISTS comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    issue_id INT NOT NULL,
    title VARCHAR(255),
    user_id INT NOT NULL,
    date DATETIME,
    content TEXT,
    FOREIGN KEY (issue_id) REFERENCES issue(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    text VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Solved issues table
CREATE TABLE IF NOT EXISTS solved (
    id INT AUTO_INCREMENT PRIMARY KEY,
    issue_id INT NOT NULL,
    user_id INT NOT NULL, -- Admin who solved it?
    date DATETIME,
    FOREIGN KEY (issue_id) REFERENCES issue(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Feedback table
CREATE TABLE IF NOT EXISTS feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    solved_id INT NOT NULL,
    user_id INT NOT NULL, -- User giving feedback
    subject VARCHAR(255),
    rating INT,
    description TEXT,
    FOREIGN KEY (solved_id) REFERENCES solved(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Blocked users table
CREATE TABLE IF NOT EXISTS blocked (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert a default admin user (Password: admin123)
INSERT INTO users (name, city, email, password, status) VALUES 
('Admin', 'BetterTown', 'admin@bettertown.com', '$2a$10$sr2X2Q8mH1RlrXApj9en0euOouTKxxGYUzJUWUvnIJbAQtPoY5Gxq', 'admin');
