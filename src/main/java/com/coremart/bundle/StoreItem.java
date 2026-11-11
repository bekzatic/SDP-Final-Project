package com.coremart.bundle;

import com.coremart.model.Product;

import java.util.List;

/**
 * COMPOSITE pattern - the <b>Component</b>.
 * <p>
 * Anything a customer can put into the cart: a single product ({@link ProductItem}), a bundle
 * of products ({@link ProductBundle}) or an item with paid services wrapped around it
 * (see the {@code com.coremart.addon} package). The cart, the bundle page and the checkout
 * work with this interface only and never ask what kind of item they have.
 */
public interface StoreItem {

    /** Name shown in the cart, e.g. "Lenovo IdeaPad Slim 5" or "Игровой сетап". */
    String getTitle();

    /** Price of one item in tenge, including discounts and services. */
    long getPrice();

    /** True when every product inside the item is in stock. */
    boolean isAvailable();

    /** Lines that describe what the item consists of (bundle contents, services). */
    List<String> getDetails();

    /** All catalog products inside the item, used to check and reserve stock. */
    List<Product> getProducts();

    default String getFormattedPrice() {
        return Product.formatTenge(getPrice());
    }
}
