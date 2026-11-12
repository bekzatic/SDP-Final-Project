package com.coremart.bundle;

import com.coremart.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * COMPOSITE pattern - the <b>Composite</b>.
 * <p>
 * A bundle is a group of store items sold together at a discount. Its children can be single
 * products or other bundles, so bundles can be nested ("Игровой сетап" contains the bundle
 * "Клавиатура и мышь"). A bundle answers every {@link StoreItem} question by asking its
 * children: the price is the sum of the children's prices minus the bundle discount, and the
 * bundle is available only when every child is available.
 */
public final class ProductBundle implements StoreItem {

    public static final int MAX_DISCOUNT_PERCENT = 50;

    private final String name;
    private final int discountPercent;
    private final List<StoreItem> children = new ArrayList<>();

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

    public ProductBundle add(StoreItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item is required");
        }
        if (item == this || (item instanceof ProductBundle b && b.contains(this))) {
            throw new IllegalArgumentException("A bundle cannot contain itself");
        }
        children.add(item);
        return this;
    }

    public ProductBundle add(Product product) {
        return add(new ProductItem(product));
    }

    public boolean remove(StoreItem item) {
        return children.remove(item);
    }

    public List<StoreItem> getChildren() {
        return Collections.unmodifiableList(children);
    }

    private boolean contains(StoreItem item) {
        for (StoreItem child : children) {
            if (child == item || (child instanceof ProductBundle b && b.contains(item))) {
                return true;
            }
        }
        return false;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    /** Sum of the children's prices before the bundle discount. */
    public long getFullPrice() {
        return children.stream().mapToLong(StoreItem::getPrice).sum();
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
        return !children.isEmpty() && children.stream().allMatch(StoreItem::isAvailable);
    }

    /** Each child on its own line; the contents of nested bundles are indented with "— ". */
    @Override
    public List<String> getDetails() {
        List<String> lines = new ArrayList<>();
        for (StoreItem child : children) {
            lines.add(child.getTitle());
            for (String line : child.getDetails()) {
                lines.add("— " + line);
            }
        }
        return lines;
    }

    @Override
    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        children.forEach(child -> products.addAll(child.getProducts()));
        return products;
    }

    @Override
    public String toString() {
        return name + " " + children;
    }
}
