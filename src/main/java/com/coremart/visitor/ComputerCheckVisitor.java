package com.coremart.visitor;

import com.coremart.addon.AddOnDecorator;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;
import com.coremart.model.Category;
import com.coremart.model.Product;

/**
 * VISITOR pattern - a <b>Concrete Visitor</b>: is there a laptop or a desktop PC in the item?
 * Software setup can only be ordered for such items.
 */
public class ComputerCheckVisitor implements StoreItemVisitor<Boolean> {

    @Override
    public Boolean visitProduct(ProductItem item) {
        return isComputer(item.getProduct());
    }

    @Override
    public Boolean visitBundle(ProductBundle bundle) {
        return bundle.getContents().stream().anyMatch(ComputerCheckVisitor::isComputer);
    }

    @Override
    public Boolean visitAddOn(AddOnDecorator addOn) {
        return addOn.getInner().accept(this);
    }

    private static boolean isComputer(Product p) {
        return p.getCategory() == Category.LAPTOP || p.getCategory() == Category.DESKTOP_PC;
    }
}
