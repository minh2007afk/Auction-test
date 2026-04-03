package com.bidhub.server.model.auction;

public class AuctionResult {

    private final String auctionId;
    private final boolean hasWinner;
    private final String winnerId;
    private final double finalPrice;

    public AuctionResult(String auctionId, String winnerId, double finalPrice) {
        this.auctionId = auctionId;
        this.hasWinner = true;
        this.winnerId = winnerId;
        this.finalPrice = finalPrice;
    }

    // Dùng khi phiên đấu giá bị hủy hoặc không ai mua
    public static AuctionResult noWinner(String auctionId) {
        return new AuctionResult(auctionId);
    }

    private AuctionResult(String auctionId) {
        this.auctionId = auctionId;
        this.hasWinner = false;
        this.winnerId = null;
        this.finalPrice = 0;
    }

    public String getAuctionId() { return auctionId; }
    public boolean hasWinner() { return hasWinner; }
    public String getWinnerId() { return winnerId; }
    public double getFinalPrice() { return finalPrice; }
}