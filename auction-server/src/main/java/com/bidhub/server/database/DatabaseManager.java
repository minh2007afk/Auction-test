package com.bidhub.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối đến SQLite Database.
 * Áp dụng Singleton Pattern (Thread-safe với Double-checked locking).
 */
public class DatabaseManager {

    // Từ khóa volatile đảm bảo các luồng (thread) luôn đọc được giá trị mới nhất của instance
    private static volatile DatabaseManager instance;
    private Connection connection;

    // File database sẽ tự động được tạo ở thư mục gốc của auction-server
    private static final String DB_URL = "jdbc:sqlite:bidhub.db";

    // Constructor là PRIVATE để cấm bên ngoài gọi `new DatabaseManager()`
    private DatabaseManager() {
        try {
            // Khởi tạo kết nối duy nhất
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("[DB] Đã kết nối tới SQLite thành công!");
        } catch (SQLException e) {
            System.err.println("[DB] Lỗi kết nối tới SQLite: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Lấy instance duy nhất của DatabaseManager.
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            // Block synchronized để chống Race Condition khi có nhiều người truy cập cùng lúc
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}