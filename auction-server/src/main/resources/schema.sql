-- Bảng Người dùng
CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT 1,
    wallet_balance REAL DEFAULT 0.0,
    store_name VARCHAR(100),
    rating REAL DEFAULT 5.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Bảng Sản phẩm
CREATE TABLE IF NOT EXISTS items (
                                     id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    starting_price REAL NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    image_url VARCHAR(255),
    item_type VARCHAR(50) NOT NULL,
    brand VARCHAR(100),
    warranty_months INTEGER DEFAULT 0,
    artist_name VARCHAR(100),
    year_created INTEGER,
    make VARCHAR(100),
    mileage INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id)
    );