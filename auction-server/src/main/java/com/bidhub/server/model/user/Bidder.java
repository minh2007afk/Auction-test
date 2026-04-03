package com.bidhub.server.model.user;

import com.bidhub.server.model.enums.UserRole;

public class Bidder extends User {

    private double walletBalance; // Số dư ví (VNĐ)

    public Bidder(String username, String passwordHash, String email) {
        // Gọi lên constructor của User, tự động gán role là BIDDER
        super(username, passwordHash, email, UserRole.BIDDER);
        this.walletBalance = 0.0;
    }

    public double getWalletBalance() { return walletBalance; }

    public void addBalance(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Số tiền nạp phải > 0");
        this.walletBalance += amount;
        markUpdated(); // Nhớ gọi hàm này khi thay đổi dữ liệu!
    }

    @Override
    public String getInfo() {
        return super.getInfo() + String.format(", walletBalance=%.0f VNĐ", walletBalance);
    }
}