package com.bidhub.server.model.factory;

import com.bidhub.server.model.enums.ItemType;
import com.bidhub.server.model.item.*;
import java.util.Map;

/**
 * Factory tạo Item theo loại.
 * Áp dụng Factory Method Pattern.
 */
public final class ItemFactory {

    // Không cho phép dùng từ khóa `new` để tạo ItemFactory
    private ItemFactory() {}

    public static Item create(ItemType type, Map<String, Object> params) {
        if (type == null) throw new IllegalArgumentException("ItemType không được null");

        String name = (String) params.get("name");
        String desc = (String) params.getOrDefault("description", "");
        double price = (Double) params.get("startingPrice");
        String sellerId = (String) params.get("sellerId");

        return switch (type) {
            case ELECTRONICS -> new Electronics(
                    name, desc, price, sellerId,
                    (String) params.getOrDefault("brand", "Unknown"),
                    (Integer) params.getOrDefault("warrantyMonths", 0)
            );
            case ART -> new Art(
                    name, desc, price, sellerId,
                    (String) params.getOrDefault("artistName", "Vô danh"),
                    (Integer) params.getOrDefault("yearCreated", 2000)
            );
            case VEHICLE -> new Vehicle(
                    name, desc, price, sellerId,
                    (String) params.getOrDefault("make", "Unknown"),
                    (Integer) params.getOrDefault("mileage", 0)
            );
        };
    }
}