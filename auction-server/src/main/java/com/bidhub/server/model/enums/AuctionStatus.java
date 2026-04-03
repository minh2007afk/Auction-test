package com.bidhub.server.model.enums;

/**
 * Trạng thái vòng đời của một phiên đấu giá.
 * Áp dụng State Machine (Máy trạng thái) để kiểm soát luồng.
 */
public enum AuctionStatus {

    OPEN,       // Vừa tạo, chưa đến giờ
    RUNNING,    // Đang diễn ra, cho phép đặt giá
    FINISHED,   // Đã hết giờ, có người thắng
    PAID,       // Đã thanh toán xong
    CANCELED;   // Bị hủy (do Admin hoặc không ai mua)

    // Kiểm tra xem có được phép chuyển sang trạng thái mới không
    public boolean canTransitionTo(AuctionStatus target) {
        return switch (this) {
            case OPEN     -> target == RUNNING || target == CANCELED;
            case RUNNING  -> target == FINISHED || target == CANCELED;
            case FINISHED -> target == PAID || target == CANCELED;
            case PAID, CANCELED -> false; // Trạng thái cuối cùng, không thể đi tiếp
        };
    }

    public boolean isTerminal() {
        return this == PAID || this == CANCELED;
    }

    public boolean isActive() {
        return this == RUNNING;
    }
}