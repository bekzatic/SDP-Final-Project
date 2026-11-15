package com.coremart.bundle;

import com.coremart.catalog.ProductCatalog;
import com.coremart.kit.GamingKitFactory;
import com.coremart.kit.HomeOfficeKitFactory;
import com.coremart.kit.SetupKitFactory;
import com.coremart.kit.StudyKitFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ready-made bundles shown on the bundles page.
 * <p>
 * The client of the Abstract Factory: every bundle is assembled from one {@link SetupKitFactory},
 * so its computer, monitor and peripherals always belong to the same family. The bundles hold
 * the catalog's own product objects, so prices and stock are always current.
 */
@Component
public class BundleCatalog {

    private final Map<String, ProductBundle> bundles = new LinkedHashMap<>();

    @Autowired
    public BundleCatalog(ProductCatalog catalog) {
        this(List.of(new StudyKitFactory(catalog), new GamingKitFactory(catalog),
                new HomeOfficeKitFactory(catalog)));
    }

    BundleCatalog(List<SetupKitFactory> factories) {
        for (SetupKitFactory factory : factories) {
            bundles.put(factory.getCode(), assemble(factory));
        }
    }

    static ProductBundle assemble(SetupKitFactory factory) {
        ProductBundle bundle = new ProductBundle(factory.getTitle(), factory.getDiscountPercent())
                .add(factory.createComputer())
                .add(factory.createMonitor());
        factory.createPeripherals().forEach(bundle::add);
        return bundle;
    }

    public Map<String, ProductBundle> getAll() {
        return Collections.unmodifiableMap(bundles);
    }

    public Optional<ProductBundle> findById(String id) {
        return Optional.ofNullable(id == null ? null : bundles.get(id));
    }
}
