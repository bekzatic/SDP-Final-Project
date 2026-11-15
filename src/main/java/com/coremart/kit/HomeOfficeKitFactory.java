package com.coremart.kit;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Accessory;
import com.coremart.model.DesktopPC;
import com.coremart.model.Monitor;
import com.coremart.model.Product;

import java.util.List;

/** ABSTRACT FACTORY pattern - a <b>Concrete Factory</b>: a quiet kit for work and video calls. */
public class HomeOfficeKitFactory extends CatalogKitFactory {

    public HomeOfficeKitFactory(ProductCatalog catalog) {
        super(catalog);
    }

    @Override
    public String getCode() {
        return "home-office";
    }

    @Override
    public String getTitle() {
        return "Home office";
    }

    @Override
    public int getDiscountPercent() {
        return 6;
    }

    @Override
    public Product createComputer() {
        return find("Apple Mac mini M4", DesktopPC.class);
    }

    @Override
    public Monitor createMonitor() {
        return find("Dell P2423", Monitor.class);
    }

    @Override
    public List<Accessory> createPeripherals() {
        return List.of(find("Logitech C920 HD Pro", Accessory.class),
                find("Logitech MX Keys S", Accessory.class),
                find("Logitech M185", Accessory.class));
    }
}
