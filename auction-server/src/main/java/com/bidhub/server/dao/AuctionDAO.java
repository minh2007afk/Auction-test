package com.bidhub.server.dao;

import com.bidhub.server.database.DatabaseManager;
import com.bidhub.server.model.auction.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Class chuyên thao tác với bảng `auctions` trong Database.
 */
public class AuctionDAO implements DAO<Auction, String> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Auction save(Auction auction) {
        String sql = "INSERT INTO auctions (id, item_id, seller_id, start_time, end_time, status, current_highest_bid, total_bids) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auction.getId());
            pstmt.setString(2, auction.getItem().getId());
            pstmt.setString(3, auction.getSellerId());
            pstmt.setString(4, auction.getStartTime().format(formatter));
            pstmt.setString(5, auction.getEndTime().format(formatter));
            pstmt.setString(6, auction.getStatus().name());
            pstmt.setDouble(7, auction.getCurrentHighestBid());
            pstmt.setInt(8, auction.getTotalBids());

            pstmt.executeUpdate();
            System.out.println("[AuctionDAO] Đã lưu phiên đấu giá cho sản phẩm: " + auction.getItem().getName());
            return auction;

        } catch (SQLException e) {
            System.err.println("[AuctionDAO] Lỗi khi lưu phiên đấu giá: " + e.getMessage());
            throw new RuntimeException("Lỗi database khi lưu Auction", e);
        }
    }

    // Các hàm dưới đây tạm để trống, chúng ta sẽ code logic tìm kiếm ở tuần sau
    @Override
    public Optional<Auction> findById(String id) { return Optional.empty(); }

    @Override
    public List<Auction> findAll() { return List.of(); }

    @Override
    public Auction update(Auction entity) { return null; }

    @Override
    public boolean deleteById(String id) { return false; }
}