package com.bidhub.client.core; // Nhớ sửa package cho khớp với thư mục của bạn nếu cần

public class UserSession {
    private static String currentUser; // Biến static để lưu tên tài khoản toàn cục

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null; // Dùng khi đăng xuất
    }
}