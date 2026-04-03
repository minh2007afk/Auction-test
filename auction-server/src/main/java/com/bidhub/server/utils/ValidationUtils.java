package com.bidhub.server.utils;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Không cho phép tạo object từ class này
    private ValidationUtils() {}

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("'" + fieldName + "' không được null hoặc rỗng");
        }
    }

    public static void requireValidEmail(String email) {
        requireNotBlank(email, "email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ: '" + email + "'");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException("'" + fieldName + "' phải > 0");
        }
    }
}