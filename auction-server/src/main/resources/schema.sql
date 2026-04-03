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
-- Bảng Phiên đấu giá
CREATE TABLE IF NOT EXISTS auctions (
                                        id VARCHAR(36) PRIMARY KEY,
    item_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_highest_bid REAL,
    current_winner_id VARCHAR(36),
    total_bids INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (seller_id) REFERENCES users(id)
    );

-- Bảng Lịch sử đặt giá (Lưu lại mọi lượt bấm giá của người dùng)
CREATE TABLE IF NOT EXISTS bid_transactions (
                                                id VARCHAR(36) PRIMARY KEY,
    auction_id VARCHAR(36) NOT NULL,
    bidder_id VARCHAR(36) NOT NULL,
    amount REAL NOT NULL,
    bid_time TIMESTAMP NOT NULL,
    is_auto_bid BOOLEAN DEFAULT 0,
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
    );