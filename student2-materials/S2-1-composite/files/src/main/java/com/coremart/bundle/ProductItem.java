package com.coremart.bundle;

import com.coremart.model.Product;

import java.util.List;

/**
 * COMPOSITE pattern - a <b>Leaf</b>.
 * <p>
 * One catalog product as a {@link StoreItem}. Price and stock are read from the product
 * itself, so the item always shows the current catalog values.
 */
public final class ProductItem implements StoreItem {

    private final Product product;

    public ProductItem(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public String getTitle() {
        return product.getFullName();
    }

    @Override
    public long getPrice() {
        return product.getPrice();
    }

    @Override
    public boolean isAvailable() {
        return product.isInStock();
    }

    @Override
    public List<String> getDetails() {
        return List.of();
    }

    @Override
    public List<Product> getProducts() {
        return List.of(product);
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
