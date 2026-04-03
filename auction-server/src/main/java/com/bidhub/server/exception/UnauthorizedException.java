package com.bidhub.server.exception;

public class UnauthorizedException extends AuctionException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }

    // Factory method tạo nhanh exception
    public static UnauthorizedException sellerCannotBid() {
        return new UnauthorizedException("Người bán không thể tự đặt giá cho sản phẩm của mình");
    }
}