package com.coremart.bundle;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Ready-made bundles shown on the bundles page. Each one is a {@link ProductBundle}
 * (Composite) made of catalog products and, in two cases, a smaller nested bundle.
 * The bundles hold the catalog's own product objects, so prices and stock are always current.
 */
@Component
public class BundleCatalog {

    private final Map<String, ProductBundle> bundles = new LinkedHashMap<>();

    public BundleCatalog() {
        this(ProductCatalog.getInstance());
    }

    BundleCatalog(ProductCatalog catalog) {
        ProductBundle keyboardAndMouse = new ProductBundle("Клавиатура и мышь", 5)
                .add(product(catalog, "Logitech MX Keys S"))
                .add(product(catalog, "Logitech M185"));

        bundles.put("study", new ProductBundle("Рабочее место для учёбы", 8)
                .add(product(catalog, "Lenovo IdeaPad Slim 5"))
                .add(product(catalog, "Dell P2423"))
                .add(product(catalog, "Logitech M185")));

        bundles.put("gaming", new ProductBundle("Игровой сетап", 7)
                .add(product(catalog, "Lenovo Legion Tower 5"))
                .add(product(catalog, "LG UltraGear 27GP850"))
                .add(product(catalog, "HyperX Cloud II"))
                .add(keyboardAndMouse));

        bundles.put("home-office", new ProductBundle("Домашний офис", 6)
                .add(product(catalog, "Apple Mac mini M4"))
                .add(product(catalog, "Dell P2423"))
                .add(product(catalog, "Logitech C920 HD Pro"))
                .add(new ProductBundle("Клавиатура и мышь", 5)
                        .add(product(catalog, "Logitech MX Keys S"))
                        .add(product(catalog, "Logitech M185"))));
    }

    private static Product product(ProductCatalog catalog, String fullName) {
        return catalog.findAll().stream()
                .filter(p -> p.getFullName().equals(fullName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No product '" + fullName + "' in the catalog"));
    }

    /** Bundles by id, in the order they are shown. */
    public Map<String, ProductBundle> getAll() {
        return Collections.unmodifiableMap(bundles);
    }

    public Optional<ProductBundle> findById(String id) {
        return Optional.ofNullable(id == null ? null : bundles.get(id));
    }
}
