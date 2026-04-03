package com.bidhub.server;

import com.bidhub.server.database.MigrationRunner;
import com.bidhub.server.dao.UserDAO;
import com.bidhub.server.model.user.Bidder;

public class ServerApp {
    public static final int DEFAULT_PORT = 8888;

    public static void main(String[] args) {
        System.out.println("=== BidHub Server đang khởi động ===");

        // 1. Khởi tạo Database
        MigrationRunner.runMigrations();

        // 2. Test thử DAO: Tạo một người dùng ảo và lưu vào DB
        UserDAO userDao = new UserDAO();
        try {
            // Nhớ đổi username ở đây nếu bạn chạy file này nhiều lần, vì username có thuộc tính UNIQUE (không được trùng)
            Bidder testUser = new Bidder("techlead_01", "hashed_password_abc123", "techlead@bidhub.com");
            userDao.save(testUser);
        } catch (Exception e) {
            System.out.println("User đã tồn tại hoặc có lỗi xảy ra.");
        }

        System.out.println("BidHub Server đã chạy thành công!");
        System.out.println("Đang lắng nghe ở cổng " + DEFAULT_PORT + "...");
    }
}