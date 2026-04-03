package com.bidhub.server.model.enums;

/**
 * Vai trò của người dùng trong hệ thống.
 * Chứa logic phân quyền cơ bản: ai được đặt giá, ai được đăng bán.
 */
public enum UserRole {

    BIDDER("Người đấu giá", true, false),
    SELLER("Người bán", false, true),
    ADMIN("Quản trị viên", false, false);

    private final String displayName;
    private final boolean canBid;
    private final boolean canSell;

    UserRole(String displayName, boolean canBid, boolean canSell) {
        this.displayName = displayName;
        this.canBid = canBid;
        this.canSell = canSell;
    }

    public String getDisplayName() { return displayName; }

    // true nếu role này có quyền đặt giá
    public boolean canBid() { return canBid; }

    // true nếu role này có quyền đăng sản phẩm
    public boolean canSell() { return canSell; }

    // Dùng để parse từ database hoặc API lên
    public static UserRole fromString(String value) {
        if (value == null) throw new IllegalArgumentException("Role không được null");
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Không tồn tại role: '" + value + "'");
    }
}