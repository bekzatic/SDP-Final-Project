package com.coremart.kit;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Accessory;
import com.coremart.model.Category;
import com.coremart.model.Monitor;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupKitFactoryTest {

    private final ProductCatalog catalog = new ProductCatalog();
    private final List<SetupKitFactory> factories = List.of(new StudyKitFactory(catalog),
            new GamingKitFactory(catalog), new HomeOfficeKitFactory(catalog));

    @Test
    void everyFactoryCreatesAWholeFamily() {
        for (SetupKitFactory f : factories) {
            Product computer = f.createComputer();
            assertTrue(computer.getCategory() == Category.LAPTOP || computer.getCategory() == Category.DESKTOP_PC,
                    f.getTitle());
            assertEquals(Category.MONITOR, f.createMonitor().getCategory());
            assertFalse(f.createPeripherals().isEmpty());
            assertTrue(f.createPeripherals().stream().allMatch(a -> a.getCategory() == Category.ACCESSORY));
            assertTrue(f.getDiscountPercent() > 0 && f.getDiscountPercent() < 50);
        }
    }

    @Test
    void familiesDoNotMix() {
        Monitor gamingMonitor = new GamingKitFactory(catalog).createMonitor();
        Monitor studyMonitor = new StudyKitFactory(catalog).createMonitor();
        assertTrue(gamingMonitor.getRefreshRateHz() > studyMonitor.getRefreshRateHz(),
                "the gaming kit gets a faster monitor");
        List<Accessory> gamingParts = new GamingKitFactory(catalog).createPeripherals();
        assertTrue(gamingParts.stream().anyMatch(a -> a.getFullName().contains("HyperX")));
    }

    @Test
    void partsAreTheCatalogsOwnProducts() {
        Product computer = new StudyKitFactory(catalog).createComputer();
        assertSame(computer, catalog.findById(computer.getId()).orElseThrow());
    }

    @Test
    void codesAreUnique() {
        assertEquals(factories.size(), factories.stream().map(SetupKitFactory::getCode).distinct().count());
    }

    @Test
    void missingProductIsReported() {
        CatalogKitFactory broken = new StudyKitFactory(catalog) {
            @Override
            public Product createComputer() {
                return find("No Such Laptop", Product.class);
            }
        };
        assertThrows(IllegalStateException.class, broken::createComputer);
    }
}
