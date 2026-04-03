package com.bidhub.server.model.item;

import com.bidhub.server.model.enums.ItemType;

public class Art extends Item {
    private String artistName;
    private int yearCreated;

    public Art(String name, String description, double startingPrice,
               String sellerId, String artistName, int yearCreated) {
        super(name, description, startingPrice, sellerId, ItemType.ART);
        this.artistName = artistName != null ? artistName : "Vô danh";
        this.yearCreated = yearCreated;
    }

    @Override
    public String getDetailedInfo() {
        return String.format("Tác giả: %s | Năm: %d", artistName, yearCreated);
    }
}