package com.coremart.kit;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;

/** Shared base of the concrete factories: the parts of every kit are real catalog products. */
abstract class CatalogKitFactory implements SetupKitFactory {

    private final ProductCatalog catalog;

    protected CatalogKitFactory(ProductCatalog catalog) {
        this.catalog = catalog;
    }

    protected <T extends Product> T find(String fullName, Class<T> type) {
        return catalog.findAll().stream()
                .filter(p -> p.getFullName().equals(fullName))
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No " + type.getSimpleName() + " '" + fullName
                        + "' in the catalog"));
    }
}
