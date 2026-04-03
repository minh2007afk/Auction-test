package com.bidhub.server.exception;

public class InvalidBidException extends AuctionException {
    public InvalidBidException(double rejectedAmount, double minimumRequired) {
        super(String.format("Giá đặt %.0f VNĐ không hợp lệ. Tối thiểu phải là: %.0f VNĐ",
                rejectedAmount, minimumRequired), "INVALID_BID");
    }
}