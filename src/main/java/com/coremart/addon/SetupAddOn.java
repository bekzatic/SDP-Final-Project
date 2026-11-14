package com.coremart.addon;

import com.coremart.bundle.StoreItem;
import com.coremart.model.Category;

/**
 * DECORATOR pattern - a <b>Concrete Decorator</b>: installing Windows, drivers and programs.
 * <p>
 * Only makes sense for a computer, so the wrapped item must contain a laptop or a desktop PC.
 */
public class SetupAddOn extends AddOnDecorator {

    public static final long PRICE = 15_000;

    public SetupAddOn(StoreItem inner) {
        super(inner);
        if (!containsComputer(inner)) {
            throw new IllegalArgumentException("Software setup is only available for laptops and desktop PCs");
        }
    }

    public static boolean containsComputer(StoreItem item) {
        return item.getProducts().stream().anyMatch(p ->
                p.getCategory() == Category.LAPTOP || p.getCategory() == Category.DESKTOP_PC);
    }

    @Override
    public String getAddOnName() {
        return "Software setup";
    }

    @Override
    public long getAddOnPrice() {
        return PRICE;
    }
}
