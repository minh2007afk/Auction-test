package com.bidhub.server.model.auction;

import com.bidhub.server.model.entity.Entity;
import com.bidhub.server.model.enums.AuctionStatus;
import com.bidhub.server.model.item.Item;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Objects;

public class Auction extends Entity {

    private final Item item;
    private final String sellerId;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;

    private double currentHighestBid;
    private String currentWinnerId; // ID của người đang trả giá cao nhất
    private int totalBids;

    public Auction(Item item, String sellerId, LocalDateTime endTime) {
        super();
        this.item = Objects.requireNonNull(item, "Item không được null");
        this.sellerId = Objects.requireNonNull(sellerId, "SellerId không được null");
        if (endTime == null || endTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải ở trong tương lai");
        }
        this.startTime = LocalDateTime.now();
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN; // Mặc định khi mới tạo là OPEN
        this.currentHighestBid = item.getStartingPrice();
        this.currentWinnerId = null;
        this.totalBids = 0;
    }

    // ===================== Getters =====================
    public Item getItem() { return item; }
    public String getSellerId() { return sellerId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public AuctionStatus getStatus() { return status; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public String getCurrentWinnerId() { return currentWinnerId; }
    public int getTotalBids() { return totalBids; }

    // ===================== Business Logic =====================

    public boolean isActive() {
        return status == AuctionStatus.RUNNING && endTime.isAfter(LocalDateTime.now());
    }

    // Kiểm tra xem User này có hợp lệ để đặt giá không
    public boolean canAcceptBid(String bidderId) {
        if (!isActive()) return false;
        if (sellerId.equals(bidderId)) return false; // Seller không được tự mua đồ của mình
        return true;
    }

    // Cập nhật giá khi có người đặt giá cao hơn
    public void updateHighestBid(String bidderId, double amount) {
        if (amount <= currentHighestBid) {
            throw new IllegalArgumentException("Giá đặt phải cao hơn giá hiện tại");
        }
        this.currentHighestBid = amount;
        this.currentWinnerId = bidderId;
        this.totalBids++;
        markUpdated();
    }

    // Đổi trạng thái (sử dụng State Machine để kiểm tra)
    public void transitionTo(AuctionStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("Không thể chuyển từ " + status + " sang " + newStatus);
        }
        this.status = newStatus;
        markUpdated();
    }

    @Override
    public String getInfo() {
        return String.format("Auction[%s] item='%s' | Giá: %.0f VNĐ | Status: %s",
                getId().substring(0, 8), item.getName(), currentHighestBid, status);
    }
}