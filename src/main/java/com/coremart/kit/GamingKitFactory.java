package com.coremart.kit;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Accessory;
import com.coremart.model.DesktopPC;
import com.coremart.model.Monitor;
import com.coremart.model.Product;

import java.util.List;

/** ABSTRACT FACTORY pattern - a <b>Concrete Factory</b>: a gaming PC with gaming parts. */
public class GamingKitFactory extends CatalogKitFactory {

    public GamingKitFactory(ProductCatalog catalog) {
        super(catalog);
    }

    @Override
    public String getCode() {
        return "gaming";
    }

    @Override
    public String getTitle() {
        return "Gaming setup";
    }

    @Override
    public int getDiscountPercent() {
        return 7;
    }

    @Override
    public Product createComputer() {
        return find("Lenovo Legion Tower 5", DesktopPC.class);
    }

    @Override
    public Monitor createMonitor() {
        return find("LG UltraGear 27GP850", Monitor.class);
    }

    @Override
    public List<Accessory> createPeripherals() {
        return List.of(find("HyperX Cloud II", Accessory.class),
                find("Logitech MX Keys S", Accessory.class),
                find("Logitech M185", Accessory.class));
    }
}
