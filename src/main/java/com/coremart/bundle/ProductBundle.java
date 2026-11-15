package com.coremart.bundle;

import com.coremart.model.Product;
import com.coremart.visitor.StoreItemVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VISITOR pattern - a <b>Concrete Element</b>: several products sold together at a discount.
 * The price is the sum of the products' prices minus the bundle discount, and the bundle is
 * available only when every product is in stock.
 */
public final class ProductBundle implements StoreItem {

    public static final int MAX_DISCOUNT_PERCENT = 50;

    private final String name;
    private final int discountPercent;
    private final List<Product> contents = new ArrayList<>();

    public ProductBundle(String name, int discountPercent) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bundle name is required");
        }
        if (discountPercent < 0 || discountPercent > MAX_DISCOUNT_PERCENT) {
            throw new IllegalArgumentException("Discount must be 0-" + MAX_DISCOUNT_PERCENT + "%");
        }
        this.name = name;
        this.discountPercent = discountPercent;
    }

    public ProductBundle add(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }
        contents.add(product);
        return this;
    }

    public List<Product> getContents() {
        return Collections.unmodifiableList(contents);
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public long getFullPrice() {
        return contents.stream().mapToLong(Product::getPrice).sum();
    }

    public long getSavings() {
        return getFullPrice() - getPrice();
    }

    public String getFormattedFullPrice() {
        return Product.formatTenge(getFullPrice());
    }

    public String getFormattedSavings() {
        return Product.formatTenge(getSavings());
    }

    @Override
    public String getTitle() {
        return name;
    }

    /** Full price minus the discount, rounded down to a whole 100 ₸ in the customer's favour. */
    @Override
    public long getPrice() {
        long full = getFullPrice();
        long discounted = full - full * discountPercent / 100;
        return discountPercent == 0 ? full : discounted / 100 * 100;
    }

    @Override
    public boolean isAvailable() {
        return !contents.isEmpty() && contents.stream().allMatch(Product::isInStock);
    }

    @Override
    public <R> R accept(StoreItemVisitor<R> visitor) {
        return visitor.visitBundle(this);
    }

    @Override
    public String toString() {
        return name + " " + contents;
    }
}
