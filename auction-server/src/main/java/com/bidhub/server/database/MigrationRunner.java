package com.bidhub.server.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Trình chạy khởi tạo Database (Migration).
 * Đọc file schema.sql và thực thi các câu lệnh SQL để tạo bảng.
 */
public class MigrationRunner {

    // Không cho phép khởi tạo object, chỉ dùng hàm static
    private MigrationRunner() {}

    public static void runMigrations() {
        System.out.println("[DB] Đang kiểm tra và khởi tạo Database...");

        // Dùng try-with-resources để tự động đóng Connection và Statement sau khi dùng xong
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // Đọc file schema.sql từ thư mục resources
            InputStream is = MigrationRunner.class.getResourceAsStream("/schema.sql");
            if (is == null) {
                System.err.println("[DB] KHÔNG TÌM THẤY file schema.sql trong resources!");
                return;
            }

            // Đọc toàn bộ nội dung file thành 1 chuỗi String (String)
            String sqlScript = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            // Tách các câu lệnh SQL ra bởi dấu chấm phẩy (;)
            String[] statements = sqlScript.split(";");

            // Chạy từng câu lệnh một
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement.trim());
                }
            }

            System.out.println("[DB] Khởi tạo Database (Migration) THÀNH CÔNG!");

        } catch (Exception e) {
            System.err.println("[DB] Lỗi khi chạy Migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}