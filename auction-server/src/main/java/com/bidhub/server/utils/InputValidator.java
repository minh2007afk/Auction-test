package com.bidhub.server.utils;

import com.bidhub.server.exception.AuctionException;
import java.util.ArrayList;
import java.util.List;

public class InputValidator {

    private final String fieldName;
    private final String value;
    private final List<String> errors = new ArrayList<>();

    private InputValidator(String value, String fieldName) {
        this.value = value;
        this.fieldName = fieldName;
    }

    public static InputValidator of(String value, String fieldName) {
        return new InputValidator(value, fieldName);
    }

    // Return 'this' để cho phép chain (nối tiếp) các hàm
    public InputValidator notBlank() {
        if (value == null || value.isBlank()) {
            errors.add("'" + fieldName + "' không được để trống");
        }
        return this;
    }

    public InputValidator minLength(int min) {
        if (value != null && value.length() < min) {
            errors.add("'" + fieldName + "' phải có ít nhất " + min + " ký tự");
        }
        return this;
    }

    // Nếu có lỗi, ném ra ngay lập tức
    public void validate() {
        if (!errors.isEmpty()) {
            throw new AuctionException(errors.get(0), "VALIDATION_ERROR");
        }
    }
}