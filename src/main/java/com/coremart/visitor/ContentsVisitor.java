package com.coremart.visitor;

import com.coremart.addon.AddOnDecorator;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;
import com.coremart.model.Product;

import java.util.List;

/** VISITOR pattern - a <b>Concrete Visitor</b>: the catalog products inside an item, for stock checks. */
public class ContentsVisitor implements StoreItemVisitor<List<Product>> {

    @Override
    public List<Product> visitProduct(ProductItem item) {
        return List.of(item.getProduct());
    }

    @Override
    public List<Product> visitBundle(ProductBundle bundle) {
        return bundle.getContents();
    }

    @Override
    public List<Product> visitAddOn(AddOnDecorator addOn) {
        return addOn.getInner().accept(this);
    }
}
