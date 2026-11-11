package com.coremart.model;

/** Product categories of the store, with the names shown to customers. */
public enum Category {
    LAPTOP("Ноутбуки"),
    DESKTOP_PC("Готовые ПК"),
    MONITOR("Мониторы"),
    ACCESSORY("Аксессуары");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
