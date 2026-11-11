package com.coremart.factory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The raw data a {@link ProductFactory} turns into a product: the common fields of every
 * product plus type-specific attributes (for example "cpu" or "refreshRateHz").
 */
public class ProductSpec {

    private final String name;
    private final String brand;
    private final long price;
    private final int stock;
    private final String description;
    private final Map<String, Object> attributes;

    public ProductSpec(String name, String brand, long price, int stock, String description,
                       Map<String, Object> attributes) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.attributes = new LinkedHashMap<>(attributes == null ? Map.of() : attributes);
    }

    public String getName() { return name; }
    public String getBrand() { return brand; }
    public long getPrice() { return price; }
    public int getStock() { return stock; }
    public String getDescription() { return description; }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public boolean has(String key) {
        return attributes.get(key) != null;
    }

    public String text(String key) {
        Object value = attributes.get(key);
        if (!(value instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("Attribute '" + key + "' must be a non-empty text");
        }
        return s.trim();
    }

    public int integer(String key) {
        Object value = attributes.get(key);
        if (!(value instanceof Integer i) || i <= 0) {
            throw new IllegalArgumentException("Attribute '" + key + "' must be a positive whole number");
        }
        return i;
    }

    public double decimal(String key) {
        Object value = attributes.get(key);
        if (!(value instanceof Number n) || n.doubleValue() <= 0) {
            throw new IllegalArgumentException("Attribute '" + key + "' must be a positive number");
        }
        return n.doubleValue();
    }
}
