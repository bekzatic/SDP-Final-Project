package com.coremart.visitor;

import com.coremart.addon.AddOnDecorator;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;

/**
 * VISITOR pattern - the <b>Visitor</b>.
 * <p>
 * One operation over the kinds of store items, with a method for each kind. A new operation
 * is a new visitor class; the item classes do not change. {@code R} is the result type.
 */
public interface StoreItemVisitor<R> {

    R visitProduct(ProductItem item);

    R visitBundle(ProductBundle bundle);

    R visitAddOn(AddOnDecorator addOn);
}
