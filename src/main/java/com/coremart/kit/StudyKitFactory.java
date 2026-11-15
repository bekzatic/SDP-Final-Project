package com.coremart.kit;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Accessory;
import com.coremart.model.Laptop;
import com.coremart.model.Monitor;
import com.coremart.model.Product;

import java.util.List;

/** ABSTRACT FACTORY pattern - a <b>Concrete Factory</b>: an affordable kit for study. */
public class StudyKitFactory extends CatalogKitFactory {

    public StudyKitFactory(ProductCatalog catalog) {
        super(catalog);
    }

    @Override
    public String getCode() {
        return "study";
    }

    @Override
    public String getTitle() {
        return "Study desk setup";
    }

    @Override
    public int getDiscountPercent() {
        return 8;
    }

    @Override
    public Product createComputer() {
        return find("Lenovo IdeaPad Slim 5", Laptop.class);
    }

    @Override
    public Monitor createMonitor() {
        return find("Dell P2423", Monitor.class);
    }

    @Override
    public List<Accessory> createPeripherals() {
        return List.of(find("Logitech M185", Accessory.class));
    }
}
