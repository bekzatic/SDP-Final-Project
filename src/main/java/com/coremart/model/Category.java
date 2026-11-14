package com.coremart.model;

/** Product categories of the store, with the names shown to customers. */
public enum Category {
    LAPTOP("Laptops"),
    DESKTOP_PC("Desktop PCs"),
    MONITOR("Monitors"),
    ACCESSORY("Accessories");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
