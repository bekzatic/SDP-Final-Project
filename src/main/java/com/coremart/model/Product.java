package com.coremart.model;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Base class of everything the store sells.
 * <p>
 * Concrete products ({@link Laptop}, {@link DesktopPC}, {@link Monitor}, {@link Accessory})
 * add their own characteristics and describe them in {@link #getSpecs()}.
 * Prices are whole tenge.
 */
public abstract class Product {

    private final long id;
    private final String name;
    private final String brand;
    private final long price;
    private final String description;
    private int stock;

    protected Product(long id, String name, String brand, long price, int stock, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Product brand is required");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.id = id;
        this.name = name.trim();
        this.brand = brand.trim();
        this.price = price;
        this.stock = stock;
        this.description = description == null ? "" : description.trim();
    }

    public abstract Category getCategory();

    /** Characteristics in the order they are shown on the product page (label -> value). */
    public abstract Map<String, String> getSpecs();

    public long getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public long getPrice() { return price; }
    public String getDescription() { return description; }
    public int getStock() { return stock; }

    public boolean isInStock() {
        return stock > 0;
    }

    /** Stock changes go through the catalog; the value can never become negative. */
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = stock;
    }

    public String getFullName() {
        return brand + " " + name;
    }

    public String getFormattedPrice() {
        return formatTenge(price);
    }

    public static String formatTenge(long amount) {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        return format.format(amount).replace(' ', ' ').replace(' ', ' ') + " ₸";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Product other && other.id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getFullName() + " (" + getFormattedPrice() + ")";
    }
}
