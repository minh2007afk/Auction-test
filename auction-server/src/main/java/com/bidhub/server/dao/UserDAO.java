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

    /**
     * Kiểm tra tài khoản và mật khẩu trong Database
     */
    public com.bidhub.server.model.user.User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (java.sql.Connection conn = com.bidhub.server.database.DatabaseManager.getInstance().getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                // Nếu rs.next() có dữ liệu nghĩa là tìm thấy tài khoản hợp lệ
                if (rs.next()) {
                    String role = rs.getString("role");
                    String email = rs.getString("email");

                    // Phục dựng lại đối tượng User từ dữ liệu trong DB
                    if ("SELLER".equals(role)) {
                        return new com.bidhub.server.model.user.Seller(username, password, email);
                    } else {
                        return new com.bidhub.server.model.user.Bidder(username, password, email);
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[UserDAO] Lỗi khi xác thực đăng nhập: " + e.getMessage());
        }

        // Trả về null nếu không tìm thấy (sai tài khoản hoặc mật khẩu)
        return null;
    }

    /**
     * Hàm cộng tiền vào ví cho người dùng
     */
    public boolean updateBalance(String username, double amount) {
        try (java.sql.Connection conn = com.bidhub.server.database.DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            // 1. "BẢO KÊ" TÀI KHOẢN: Nếu chưa có thì tự động tạo luôn (Tránh lỗi xóa DB)
            stmt.execute("INSERT OR IGNORE INTO users (id, username, password_hash, role, wallet_balance) VALUES ('u_auto', '" + username + "', '123', 'BIDDER', 0.0)");

            // 2. CỘNG TIỀN VÀO VÍ
            String sql = "UPDATE users SET wallet_balance = wallet_balance + ? WHERE username = ?";
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, username);
                return pstmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi nạp tiền: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy số dư ví hiện tại của người dùng
     */
    public double getBalance(String username) {
        String sql = "SELECT wallet_balance FROM users WHERE username = ?";
        try (java.sql.Connection conn = com.bidhub.server.database.DatabaseManager.getInstance().getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("wallet_balance");
                }
            }
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi lấy số dư: " + e.getMessage());
        }
        return 0.0; // Trả về 0 nếu có lỗi hoặc không tìm thấy
    }
}