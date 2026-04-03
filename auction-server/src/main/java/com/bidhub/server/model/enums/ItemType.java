package com.bidhub.server.model.enums;

/**
 * Loại sản phẩm đấu giá trong hệ thống.
 */
public enum ItemType {
    ELECTRONICS("Thiết bị điện tử"),
    ART("Tác phẩm nghệ thuật"),
    VEHICLE("Phương tiện");

    private final String displayName;

    ItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}