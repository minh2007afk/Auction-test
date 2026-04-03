package com.bidhub.server.model.auction;

import com.bidhub.server.model.entity.Entity;
import java.time.LocalDateTime;
import java.util.Objects;

public class BidTransaction extends Entity {

    private final String auctionId;
    private final String bidderId;
    private final double amount;
    private final LocalDateTime bidTime;
    private final boolean isAutoBid;

    public BidTransaction(String auctionId, String bidderId, double amount, boolean isAutoBid) {
        super();
        this.auctionId = Objects.requireNonNull(auctionId);
        this.bidderId  = Objects.requireNonNull(bidderId);
        if (amount <= 0) throw new IllegalArgumentException("Số tiền đặt phải > 0");
        this.amount    = amount;
        this.bidTime   = LocalDateTime.now();
        this.isAutoBid = isAutoBid;
    }

    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
    public double getAmount() { return amount; }
    public LocalDateTime getBidTime() { return bidTime; }
    public boolean isAutoBid() { return isAutoBid; }

    @Override
    public String getInfo() {
        return String.format("Bid[%.0f VNĐ] bởi %s lúc %s", amount, bidderId.substring(0, 8), bidTime);
    }
}