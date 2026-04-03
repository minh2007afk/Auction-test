package com.bidhub.server.model.user;

import com.bidhub.server.model.enums.UserRole;

public class Seller extends User {

    private String storeName;
    private double rating;

    public Seller(String username, String passwordHash, String email, String storeName) {
        // Gán cứng role là SELLER
        super(username, passwordHash, email, UserRole.SELLER);
        this.storeName = storeName != null ? storeName : username + "'s Store";
        this.rating = 5.0; // Điểm mặc định khi mới tham gia
    }

    public String getStoreName() { return storeName; }
    public double getRating() { return rating; }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
        markUpdated();
    }

    @Override
    public String getInfo() {
        return super.getInfo() + String.format(", store='%s', rating=%.1f", storeName, rating);
    }
}