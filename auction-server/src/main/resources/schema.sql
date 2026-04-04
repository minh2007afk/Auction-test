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
-- Dữ liệu mẫu (Tự bỏ qua nếu đã tồn tại)
INSERT OR IGNORE INTO users (id, username, password_hash, email, role) VALUES ('u_admin', 'admin', '123', 'admin@gmail.com', 'SELLER');

INSERT OR IGNORE INTO items (id, name, description, starting_price) VALUES ('item_1', 'iPhone 15 Pro Max', 'Hàng VN/A', 25000000.0);
INSERT OR IGNORE INTO items (id, name, description, starting_price) VALUES ('item_2', 'Laptop ThinkPad X1 Carbon', 'Core i7, 16GB RAM', 18500000.0);
INSERT OR IGNORE INTO items (id, name, description, starting_price) VALUES ('item_3', 'Đồng hồ Rolex Submariner', 'Chính hãng', 150000000.0);

INSERT OR IGNORE INTO auctions (id, item_id, seller_id, start_time, end_time, status, current_highest_bid) VALUES ('auc_1', 'item_1', 'u_admin', '2026-04-01', '2026-04-30', 'Đang diễn ra', 25500000.0);
INSERT OR IGNORE INTO auctions (id, item_id, seller_id, start_time, end_time, status, current_highest_bid) VALUES ('auc_2', 'item_2', 'u_admin', '2026-04-05', '2026-04-20', 'Sắp bắt đầu', 18500000.0);
INSERT OR IGNORE INTO auctions (id, item_id, seller_id, start_time, end_time, status, current_highest_bid) VALUES ('auc_3', 'item_3', 'u_admin', '2026-03-01', '2026-03-31', 'Đã kết thúc', 160000000.0);