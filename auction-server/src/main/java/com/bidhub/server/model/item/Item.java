package com.bidhub.server.model.item;

import com.bidhub.server.model.entity.Entity;
import com.bidhub.server.model.enums.ItemType;
import java.util.Objects;

/**
 * Lớp trừu tượng đại diện cho sản phẩm được đưa ra đấu giá.
 */
public abstract class Item extends Entity {

    private String name;
    private String description;
    private double startingPrice;
    private final String sellerId; // ID của Người bán
    private String imageUrl;
    private final ItemType itemType;

    protected Item(String name, String description, double startingPrice,
                   String sellerId, ItemType itemType) {
        super();
        this.name = Objects.requireNonNull(name, "Tên sản phẩm không được null");
        if (name.isBlank()) throw new IllegalArgumentException("Tên sản phẩm không được rỗng");
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Giá khởi điểm phải > 0");
        }
        this.description = description != null ? description : "";
        this.startingPrice = startingPrice;
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId không được null");
        this.itemType = Objects.requireNonNull(itemType, "itemType không được null");
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public String getSellerId() { return sellerId; }
    public String getImageUrl() { return imageUrl; }
    public ItemType getItemType() { return itemType; }

    // Setters
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Tên không được rỗng");
        this.name = name;
        markUpdated();
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        markUpdated();
    }

    // Phương thức trừu tượng: Yêu cầu các class con phải cung cấp thông tin đặc thù
    public abstract String getDetailedInfo();

    @Override
    public String getInfo() {
        return String.format("[%s] %s | Giá khởi điểm: %.0f VNĐ",
                itemType.getDisplayName(), name, startingPrice);
    }
}