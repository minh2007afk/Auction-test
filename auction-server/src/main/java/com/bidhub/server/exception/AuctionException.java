package com.bidhub.server.exception;

/**
 * Base exception cho tất cả lỗi nghiệp vụ của hệ thống BidHub.
 * Kế thừa RuntimeException để không bắt buộc phải try-catch ở mọi nơi.
 */
public class AuctionException extends RuntimeException {

    private final String errorCode; // Mã lỗi để Client dễ dàng hiển thị giao diện (VD: "INVALID_BID")

    public AuctionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}