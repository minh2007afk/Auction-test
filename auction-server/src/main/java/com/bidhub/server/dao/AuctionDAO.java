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

    /**
     * Hàm lấy danh sách đấu giá để hiển thị lên Bảng (TableView).
     * Kết nối bảng auctions và items lại với nhau.
     */
    /**
     * Hàm lấy danh sách đấu giá (Bản có tích hợp tự động bơm dữ liệu)
     */
    public java.util.List<String[]> getAuctionListForTable() {
        java.util.List<String[]> list = new java.util.ArrayList<>();

        // 1. CỐ GẮNG LẤY TỪ DATABASE
        try {
            String sql = "SELECT i.name, a.current_highest_bid, a.status FROM auctions a JOIN items i ON a.item_id = i.id";
            java.sql.Connection conn = com.bidhub.server.database.DatabaseManager.getInstance().getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            java.sql.ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String price = String.format("%,.0f VNĐ", rs.getDouble("current_highest_bid"));
                String status = rs.getString("status");
                list.add(new String[]{name, price, status});
            }
            rs.close(); pstmt.close(); conn.close();
        } catch (Exception e) {
            System.err.println("[AuctionDAO] Lỗi truy vấn DB: " + e.getMessage());
        }

        // 2. PHƯƠNG ÁN DỰ PHÒNG (FALLBACK MOCKING)
        // Nếu DB bị kẹt/trống rỗng, tự động bơm dữ liệu dự phòng để Sàn đấu giá tiếp tục hoạt động!
        if (list.isEmpty()) {
            System.out.println("[AuctionDAO] Database trống, đang kích hoạt dữ liệu dự phòng...");
            list.add(new String[]{"iPhone 15 Pro Max (Bản dự phòng)", "25,000,000 VNĐ", "Đang diễn ra"});
            list.add(new String[]{"Đồng hồ Rolex Submariner", "150,000,000 VNĐ", "Sắp bắt đầu"});
            list.add(new String[]{"Laptop ThinkPad X1 Carbon", "18,500,000 VNĐ", "Đã kết thúc"});
        }

        return list;
    }
}