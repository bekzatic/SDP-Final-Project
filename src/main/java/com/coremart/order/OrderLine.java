package com.coremart.order;

import com.coremart.bundle.StoreItem;
import com.coremart.model.Product;

import java.util.List;

/**
 * A line of a placed order. It copies the title, services and price from the cart, so later
 * changes in the catalog do not change orders that are already placed.
 */
public final class OrderLine {

    private final String title;
    private final List<String> details;
    private final long unitPrice;
    private final int quantity;
    private final List<Product> products;

    public OrderLine(String title, List<String> details, long unitPrice, int quantity, List<Product> products) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Line title is required");
        }
        if (unitPrice <= 0 || quantity <= 0) {
            throw new IllegalArgumentException("Price and quantity must be positive");
        }
        this.title = title;
        this.details = List.copyOf(details == null ? List.of() : details);
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.products = List.copyOf(products == null ? List.of() : products);
    }

    /** Copies a cart line: works for products, bundles and items with services alike. */
    public static OrderLine of(StoreItem item, int quantity) {
        return new OrderLine(item.getTitle(), item.getDetails(), item.getPrice(), quantity, item.getProducts());
    }

    public String getTitle() { return title; }
    public List<String> getDetails() { return details; }
    public long getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    /** Products inside one unit of this line (several for a bundle). */
    public List<Product> getProducts() { return products; }

    public long getTotal() {
        return unitPrice * quantity;
    }

    public String getFormattedUnitPrice() {
        return Product.formatTenge(unitPrice);
    }

    public String getFormattedTotal() {
        return Product.formatTenge(getTotal());
    }
}
