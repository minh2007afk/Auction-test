package com.bidhub.server.model.item;

import com.bidhub.server.model.enums.ItemType;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronics(String name, String description, double startingPrice,
                       String sellerId, String brand, int warrantyMonths) {
        super(name, description, startingPrice, sellerId, ItemType.ELECTRONICS);
        this.brand = brand != null ? brand : "Unknown";
        this.warrantyMonths = Math.max(0, warrantyMonths);
    }

    @Override
    public String getDetailedInfo() {
        return String.format("Hãng: %s | Bảo hành: %d tháng", brand, warrantyMonths);
    }
}