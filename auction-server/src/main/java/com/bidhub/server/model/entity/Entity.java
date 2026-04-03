package com.bidhub.server.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Lớp cơ sở trừu tượng cho tất cả các entity trong hệ thống.
 * Chứa các trường chung: id, createdAt, updatedAt.
 */
public abstract class Entity {

    private final String id;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor tự sinh UUID và thời gian tạo
    protected Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor dùng khi lấy dữ liệu từ Database lên
    protected Entity(String id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Entity id không được null hoặc rỗng");
        }
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public String getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Phương thức này bắt buộc phải gọi mỗi khi cập nhật dữ liệu của Entity
    protected void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    // Abstract method: Ép các class con phải tự định nghĩa cách hiển thị thông tin
    public abstract String getInfo();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}