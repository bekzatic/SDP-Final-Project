package com.coremart.factory;

import com.coremart.model.Category;
import com.coremart.model.Product;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FACTORY METHOD pattern - the <b>Creator</b>.
 * <p>
 * {@link #create(ProductSpec)} is the fixed procedure every product goes through:
 * check that the type-specific attributes are present, give the product a store-wide
 * unique id and let the subclass build it. The one step that differs per product type,
 * {@link #createProduct(long, ProductSpec)}, is the <i>factory method</i> that each
 * concrete creator ({@link LaptopFactory}, {@link DesktopPCFactory}, {@link MonitorFactory},
 * {@link AccessoryFactory}) overrides. Client code works with {@code ProductFactory} and
 * {@link Product} only and never calls a concrete product constructor.
 */
public abstract class ProductFactory {

    private static final AtomicLong NEXT_ID = new AtomicLong(1);

    /** Template method: validate, assign an id, then call the factory method. */
    public final Product create(ProductSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("Product data is required");
        }
        for (String key : requiredAttributes()) {
            if (!spec.has(key)) {
                throw new IllegalArgumentException(getCategory().getDisplayName()
                        + ": attribute '" + key + "' is required");
            }
        }
        return createProduct(NEXT_ID.getAndIncrement(), spec);
    }

    /** The factory method: builds the concrete product of this creator's type. */
    protected abstract Product createProduct(long id, ProductSpec spec);

    /** Attribute keys this product type needs, checked before the factory method runs. */
    public abstract List<String> requiredAttributes();

    public abstract Category getCategory();

    public static ProductFactory forCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        switch (category) {
            case LAPTOP:     return new LaptopFactory();
            case DESKTOP_PC: return new DesktopPCFactory();
            case MONITOR:    return new MonitorFactory();
            case ACCESSORY:  return new AccessoryFactory();
            default: throw new IllegalArgumentException("No factory for " + category);
        }
    }
}
