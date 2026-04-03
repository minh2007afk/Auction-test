package com.bidhub.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối đến SQLite Database.
 * Áp dụng Singleton Pattern để đảm bảo chỉ có 1 Manager.
 */
public class DatabaseManager {

    private static volatile DatabaseManager instance;

    // File database sẽ tự động được tạo ở thư mục gốc
    private static final String DB_URL = "jdbc:sqlite:bidhub.db";

    private DatabaseManager() {
        // Không mở Connection sẵn ở đây nữa, chỉ đăng ký Driver (nếu cần)
        System.out.println("[DB] DatabaseManager đã khởi tạo.");
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Cấp phát một kết nối MỚI mỗi khi được gọi.
     * DAO sử dụng xong phải tự đóng (khuyến nghị dùng try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}