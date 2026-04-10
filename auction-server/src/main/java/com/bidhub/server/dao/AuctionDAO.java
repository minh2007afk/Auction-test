package com.bidhub.server.dao;

import com.bidhub.server.database.DatabaseManager;
import com.bidhub.server.model.auction.Auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
            return auction;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi database khi lưu Auction", e);
        }
    }

    @Override
    public Optional<Auction> findById(String id) { return Optional.empty(); }

    @Override
    public List<Auction> findAll() { return List.of(); }

    @Override
    public Auction update(Auction entity) { return null; }

    @Override
    public boolean deleteById(String id) { return false; }

    /**
     * ĐÃ NÂNG CẤP: Lấy thêm Mô tả (description) và Ảnh (image_path)
     */
    public java.util.List<String[]> getAuctionListForTable() {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        try (java.sql.Connection conn = com.bidhub.server.database.DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            // Đảm bảo các cột mới tồn tại
            try { stmt.execute("ALTER TABLE items ADD COLUMN description TEXT"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE items ADD COLUMN image_path TEXT"); } catch (Exception ignored) {}

            stmt.execute("INSERT OR IGNORE INTO users (id, username, password_hash, role, wallet_balance) VALUES ('u_admin', 'admin', '123', 'SELLER', 0.0)");

            // Cập nhật dữ liệu mẫu có thêm cột mô tả và ảnh
            stmt.execute("INSERT OR IGNORE INTO items (id, name, starting_price, seller_id, item_type, description, image_path) VALUES ('it_1', 'iPhone 15 Pro Max', 25000000.0, 'admin', 'Điện thoại', 'Hàng mới nguyên seal 100%', 'no_image.png')");
            stmt.execute("INSERT OR IGNORE INTO items (id, name, starting_price, seller_id, item_type, description, image_path) VALUES ('it_2', 'Đồng hồ Rolex Submariner', 150000000.0, 'admin', 'Thời trang', 'Đồng hồ cơ Thụy Sĩ chính hãng', 'no_image.png')");

            // NÂNG CẤP: Dữ liệu mẫu thêm phần giờ phút giây (HH:mm:ss) cho chuẩn form
            stmt.execute("INSERT OR IGNORE INTO auctions (id, item_id, seller_id, start_time, end_time, status, current_highest_bid) VALUES ('au_1', 'it_1', 'admin', '2026-04-01 08:00:00', '2026-05-30 20:00:00', 'Đang diễn ra', 25000000.0)");
            stmt.execute("INSERT OR IGNORE INTO auctions (id, item_id, seller_id, start_time, end_time, status, current_highest_bid) VALUES ('au_2', 'it_2', 'admin', '2026-04-05 09:00:00', '2026-06-20 22:30:00', 'Sắp bắt đầu', 150000000.0)");

            // Lấy cả description và image_path
            String sql = "SELECT i.name, a.current_highest_bid, a.status, a.end_time, i.description, i.image_path FROM auctions a JOIN items i ON a.item_id = i.id";
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new String[]{
                            rs.getString("name"),
                            String.format("%,.0f VNĐ", rs.getDouble("current_highest_bid")),
                            rs.getString("status"),
                            rs.getString("end_time"),
                            rs.getString("description"),
                            rs.getString("image_path")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String placeBid(String username, String itemName, double bidAmount) {
        try (java.sql.Connection conn = com.bidhub.server.database.DatabaseManager.getInstance().getConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE auctions ADD COLUMN highest_bidder TEXT");
            } catch (Exception ignored) {}

            conn.setAutoCommit(false);
            try {
                double balance = 0;
                String checkUserSql = "SELECT wallet_balance FROM users WHERE username = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(checkUserSql)) {
                    pstmt.setString(1, username);
                    java.sql.ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) return "Tài khoản không tồn tại!";
                    balance = rs.getDouble("wallet_balance");
                }
                if (balance < bidAmount) return "Số dư không đủ!";

                String checkAuctionSql = "SELECT a.id, a.current_highest_bid, a.status, a.highest_bidder FROM auctions a JOIN items i ON a.item_id = i.id WHERE i.name = ?";
                String auctionId = "";
                double currentBid = 0;
                String previousBidder = "";

                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(checkAuctionSql)) {
                    pstmt.setString(1, itemName);
                    java.sql.ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) return "Không tìm thấy sản phẩm!";
                    auctionId = rs.getString("id");
                    currentBid = rs.getDouble("current_highest_bid");
                    previousBidder = rs.getString("highest_bidder");
                    if (!"Đang diễn ra".equals(rs.getString("status"))) return "Đấu giá đã kết thúc!";
                }

                if (bidAmount <= currentBid) return "Giá phải cao hơn " + currentBid;
                if (username.equals(previousBidder)) return "Bạn đang dẫn đầu!";

                String deductSql = "UPDATE users SET wallet_balance = wallet_balance - ? WHERE username = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(deductSql)) {
                    pstmt.setDouble(1, bidAmount);
                    pstmt.setString(2, username);
                    pstmt.executeUpdate();
                }

                if (previousBidder != null && !previousBidder.isEmpty()) {
                    String refundSql = "UPDATE users SET wallet_balance = wallet_balance + ? WHERE username = ?";
                    try (java.sql.PreparedStatement pstmt = conn.prepareStatement(refundSql)) {
                        pstmt.setDouble(1, currentBid);
                        pstmt.setString(2, previousBidder);
                        pstmt.executeUpdate();
                    }
                }

                String updateBidSql = "UPDATE auctions SET current_highest_bid = ?, highest_bidder = ? WHERE id = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(updateBidSql)) {
                    pstmt.setDouble(1, bidAmount);
                    pstmt.setString(2, username);
                    pstmt.setString(3, auctionId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return "SUCCESS";
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    public boolean addAuctionItem(String sellerId, String itemName, double startingPrice, String endTimeStr, String description, String imagePath) {
        try (java.sql.Connection conn = com.bidhub.server.database.DatabaseManager.getInstance().getConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE items ADD COLUMN description TEXT");
            } catch (Exception ignored) {}
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE items ADD COLUMN image_path TEXT");
            } catch (Exception ignored) {}

            conn.setAutoCommit(false);
            try {
                String itemId = java.util.UUID.randomUUID().toString();
                String auctionId = java.util.UUID.randomUUID().toString();

                String insertItem = "INSERT INTO items (id, name, starting_price, seller_id, item_type, description, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertItem)) {
                    ps.setString(1, itemId);
                    ps.setString(2, itemName);
                    ps.setDouble(3, startingPrice);
                    ps.setString(4, sellerId);
                    ps.setString(5, "Khác");
                    ps.setString(6, description);
                    ps.setString(7, imagePath);
                    ps.executeUpdate();
                }

                String insertAuction = "INSERT INTO auctions (id, item_id, seller_id, start_time, end_time, status, current_highest_bid) " +
                        "VALUES (?, ?, ?, datetime('now', 'localtime'), ?, 'Đang diễn ra', ?)";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(insertAuction)) {
                    ps.setString(1, auctionId);
                    ps.setString(2, itemId);
                    ps.setString(3, sellerId);
                    ps.setString(4, endTimeStr);
                    ps.setDouble(5, startingPrice);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * NÂNG CẤP: Chốt đơn chính xác đến từng phút, giây (yyyy-MM-dd HH:mm:ss)
     */
    public void closeExpiredAuctions() {
        // Lấy thời gian hiện tại chuẩn yyyy-MM-dd HH:mm:ss thay vì chỉ lấy ngày
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String sql = "UPDATE auctions SET status = 'Đã kết thúc' WHERE status = 'Đang diễn ra' AND end_time < ?";

        try (java.sql.Connection conn = com.bidhub.server.database.DatabaseManager.getInstance().getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, now);
            int updatedCount = pstmt.executeUpdate();

            if (updatedCount > 0) {
                System.out.println("[HỆ THỐNG] Đã tự động chốt đơn " + updatedCount + " phiên đấu giá hết hạn!");
            }
        } catch (Exception e) {
            System.err.println("[Lỗi chốt đơn] " + e.getMessage());
        }
    }
}