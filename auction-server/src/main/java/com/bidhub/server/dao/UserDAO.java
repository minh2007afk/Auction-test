package com.bidhub.server.dao;

import com.bidhub.server.database.DatabaseManager;
import com.bidhub.server.model.enums.UserRole;
import com.bidhub.server.model.user.Bidder;
import com.bidhub.server.model.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Class chuyên thao tác với bảng `users` trong Database.
 */
public class UserDAO implements DAO<User, String> {

    // Công cụ format thời gian để lưu vào SQLite
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (id, username, password_hash, email, role, active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Lấy kết nối duy nhất từ DatabaseManager
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getRole().name());
            pstmt.setBoolean(6, user.isActive());
            pstmt.setString(7, user.getCreatedAt().format(formatter));
            pstmt.setString(8, user.getUpdatedAt().format(formatter));

            pstmt.executeUpdate();
            System.out.println("[UserDAO] Đã lưu thành công user: " + user.getUsername());
            return user;

        } catch (SQLException e) {
            System.err.println("[UserDAO] Lỗi khi lưu user: " + e.getMessage());
            throw new RuntimeException("Lỗi database khi lưu user", e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        // Tạm thời để trống, chúng ta sẽ implement sau
        return Optional.empty();
    }

    @Override
    public List<User> findAll() { return List.of(); }

    @Override
    public User update(User entity) { return null; }

    @Override
    public boolean deleteById(String id) { return false; }
}