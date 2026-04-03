package com.bidhub.server.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interface chuẩn mực cho mọi Data Access Object (DAO) trong hệ thống.
 * Áp dụng Generic Pattern: T là kiểu dữ liệu (User, Item...), ID là kiểu của khóa chính (String).
 */
public interface DAO<T, ID> {

    // Lưu một đối tượng mới vào database
    T save(T entity);

    // Tìm kiếm một đối tượng theo ID (Dùng Optional để tránh lỗi NullPointerException nếu không tìm thấy)
    Optional<T> findById(ID id);

    // Lấy tất cả danh sách
    List<T> findAll();

    // Cập nhật thông tin
    T update(T entity);

    // Xóa đối tượng
    boolean deleteById(ID id);
}