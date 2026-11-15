package com.coremart.bundle;

import com.coremart.model.Product;
import com.coremart.visitor.ContentsVisitor;
import com.coremart.visitor.DetailsVisitor;
import com.coremart.visitor.StoreItemVisitor;

import java.util.List;

/**
 * VISITOR pattern - the <b>Element</b>.
 * <p>
 * Anything a customer can put into the cart: a single product ({@link ProductItem}), a bundle
 * ({@link ProductBundle}) or an item with paid services wrapped around it (see the
 * {@code com.coremart.addon} package). The cart and the checkout work with this interface only.
 * Operations that depend on the kind of item (the detail lines, the products inside, whether
 * there is a computer) are not written in these classes: they are visitors, and every item only
 * accepts a visitor and calls the method for its own type.
 */
public interface StoreItem {

    String getTitle();

    /** Price of one item in tenge, including discounts and services. */
    long getPrice();

    boolean isAvailable();

    <R> R accept(StoreItemVisitor<R> visitor);

    /** Lines that describe what the item consists of (bundle contents, services). */
    default List<String> getDetails() {
        return accept(new DetailsVisitor());
    }

    /** All catalog products inside the item, used to check and reserve stock. */
    default List<Product> getProducts() {
        return accept(new ContentsVisitor());
    }

    default String getFormattedPrice() {
        return Product.formatTenge(getPrice());
    }
}
