package com.coremart.visitor;

import com.coremart.addon.AddOnDecorator;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;
import com.coremart.model.Product;

import java.util.ArrayList;
import java.util.List;

/** VISITOR pattern - a <b>Concrete Visitor</b>: the lines shown under an item in the cart and the order. */
public class DetailsVisitor implements StoreItemVisitor<List<String>> {

    @Override
    public List<String> visitProduct(ProductItem item) {
        return List.of();
    }

    @Override
    public List<String> visitBundle(ProductBundle bundle) {
        return bundle.getContents().stream().map(Product::getFullName).toList();
    }

    @Override
    public List<String> visitAddOn(AddOnDecorator addOn) {
        List<String> lines = new ArrayList<>(addOn.getInner().accept(this));
        lines.add("+ " + addOn.getAddOnName() + " (" + Product.formatTenge(addOn.getAddOnPrice()) + ")");
        return lines;
    }
}
