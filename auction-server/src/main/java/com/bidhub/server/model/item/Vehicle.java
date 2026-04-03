package com.bidhub.server.model.item;

import com.bidhub.server.model.enums.ItemType;

public class Vehicle extends Item {
    private String make;
    private int mileage;

    public Vehicle(String name, String description, double startingPrice,
                   String sellerId, String make, int mileage) {
        super(name, description, startingPrice, sellerId, ItemType.VEHICLE);
        this.make = make != null ? make : "Unknown";
        if (mileage < 0) throw new IllegalArgumentException("Số km không thể âm");
        this.mileage = mileage;
    }

    @Override
    public String getDetailedInfo() {
        return String.format("Hãng xe: %s | Đã đi: %,d km", make, mileage);
    }
}

