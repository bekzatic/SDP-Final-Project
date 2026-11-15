package com.coremart.addon;

import com.coremart.bundle.StoreItem;
import com.coremart.visitor.StoreItemVisitor;

/**
 * DECORATOR pattern - the <b>base Decorator</b>.
 * <p>
 * A paid service (warranty, gift wrap, software setup) wraps any {@link StoreItem} - a product,
 * a bundle or an item that already has other services - and is itself a {@code StoreItem}.
 * It passes every call to the wrapped item and adds its own price, so services can be stacked
 * in any combination without a subclass for every combination:
 *
 * <pre>
 * StoreItem item = new GiftWrapAddOn(new WarrantyAddOn(new ProductItem(laptop), 2));
 * </pre>
 */
public abstract class AddOnDecorator implements StoreItem {

    protected final StoreItem inner;

    protected AddOnDecorator(StoreItem inner) {
        if (inner == null) {
            throw new IllegalArgumentException("Item to decorate is required");
        }
        this.inner = inner;
    }

    public abstract String getAddOnName();

    /** Price of the service alone, in tenge. */
    public abstract long getAddOnPrice();

    public StoreItem getInner() {
        return inner;
    }

    @Override
    public String getTitle() {
        return inner.getTitle();
    }

    @Override
    public long getPrice() {
        return inner.getPrice() + getAddOnPrice();
    }

    @Override
    public boolean isAvailable() {
        return inner.isAvailable();
    }

    @Override
    public <R> R accept(StoreItemVisitor<R> visitor) {
        return visitor.visitAddOn(this);
    }

    @Override
    public String toString() {
        return inner + " + " + getAddOnName();
    }
}
