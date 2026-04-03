package com.bidhub.server.model.user;

import com.bidhub.server.model.entity.Entity;
import com.bidhub.server.model.enums.UserRole;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lớp trừu tượng đại diện cho người dùng hệ thống.
 * Không thể tạo `new User()` trực tiếp — phải dùng lớp con cụ thể.
 */
public abstract class User extends Entity {

    private String username;
    private String passwordHash; // Tuyệt đối không lưu mật khẩu thô (plaintext)
    private String email;
    private final UserRole role;
    private boolean active;

    // Constructor dùng khi tạo tài khoản mới
    protected User(String username, String passwordHash, String email, UserRole role) {
        super(); // Gọi Entity() để sinh UUID, set timestamps
        this.username = Objects.requireNonNull(username, "username không được null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash không được null");
        this.email = Objects.requireNonNull(email, "email không được null");
        this.role = Objects.requireNonNull(role, "role không được null");
        this.active = true;
    }

    // Constructor dùng khi load từ database lên
    protected User(String id, String username, String passwordHash,
                   String email, UserRole role, boolean active,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    // ===================== Getters =====================
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }

    // ===================== Setters =====================
    // Lưu ý: mỗi khi gọi Setter, phải gọi markUpdated() để đổi thời gian
    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "email không được null");
        markUpdated();
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
        markUpdated();
    }

    public void deactivate() {
        this.active = false;
        markUpdated();
    }

    public void activate() {
        this.active = true;
        markUpdated();
    }

    // ===================== Business Logic =====================
    public boolean canBid() {
        return role.canBid() && active;
    }

    public boolean canSell() {
        return role.canSell() && active;
    }

    // Implement phương thức abstract từ Entity
    @Override
    public String getInfo() {
        return String.format("[%s] username=%s, email=%s, active=%b",
                role.getDisplayName(), username, email, active);
    }

    // Ghi đè toString, đảm bảo KHÔNG in ra mật khẩu
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{id='" + getId() + "', username='" + username + "', role=" + role + "}";
    }
}