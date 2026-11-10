package com.coremart.factory;

import com.coremart.model.Accessory;
import com.coremart.model.Category;
import com.coremart.model.DesktopPC;
import com.coremart.model.Laptop;
import com.coremart.model.Monitor;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductFactoryTest {

    private static ProductSpec spec(Map<String, Object> attributes) {
        return new ProductSpec("Model X", "Brand", 100_000, 5, "Описание", attributes);
    }

    private static final Map<String, Object> LAPTOP = Map.of("cpu", "Intel Core i5-1335U", "ramGb", 16,
            "storageGb", 512, "gpu", "Intel Iris Xe", "screenInches", 14.0, "weightKg", 1.4);
    private static final Map<String, Object> DESKTOP = Map.of("cpu", "AMD Ryzen 5 7600", "ramGb", 16,
            "storageGb", 1000, "gpu", "NVIDIA GeForce RTX 4060", "formFactor", "Mid Tower");
    private static final Map<String, Object> MONITOR = Map.of("sizeInches", 27, "resolution", "2560 × 1440",
            "refreshRateHz", 165, "panelType", "IPS");
    private static final Map<String, Object> ACCESSORY = Map.of("type", "Клавиатура", "connection", "USB-C");

    @Test
    void eachFactoryCreatesItsOwnProductType() {
        assertInstanceOf(Laptop.class, new LaptopFactory().create(spec(LAPTOP)));
        assertInstanceOf(DesktopPC.class, new DesktopPCFactory().create(spec(DESKTOP)));
        assertInstanceOf(Monitor.class, new MonitorFactory().create(spec(MONITOR)));
        assertInstanceOf(Accessory.class, new AccessoryFactory().create(spec(ACCESSORY)));
    }

    @Test
    void clientCodeOnlyNeedsTheCategory() {
        for (Category category : Category.values()) {
            ProductFactory factory = ProductFactory.forCategory(category);
            assertEquals(category, factory.getCategory());
        }
        Product monitor = ProductFactory.forCategory(Category.MONITOR).create(spec(MONITOR));
        assertEquals(Category.MONITOR, monitor.getCategory());
        assertEquals("165 Гц", monitor.getSpecs().get("Частота обновления"));
    }

    @Test
    void commonFieldsAreCopiedFromTheSpec() {
        Product laptop = new LaptopFactory().create(spec(LAPTOP));
        assertEquals("Brand Model X", laptop.getFullName());
        assertEquals(100_000, laptop.getPrice());
        assertEquals(5, laptop.getStock());
        assertEquals("Intel Core i5-1335U", ((Laptop) laptop).getCpu());
    }

    @Test
    void everyProductGetsAUniqueIncreasingId() {
        Product first = new AccessoryFactory().create(spec(ACCESSORY));
        Product second = new MonitorFactory().create(spec(MONITOR));
        assertNotEquals(first.getId(), second.getId());
        assertTrue(second.getId() > first.getId());
    }

    @Test
    void missingAttributeIsRejectedBeforeTheProductIsBuilt() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new LaptopFactory().create(spec(Map.of("cpu", "Intel Core i7"))));
        assertTrue(e.getMessage().contains("ramGb"), e.getMessage());
    }

    @Test
    void wrongAttributeTypeIsRejected() {
        Map<String, Object> wrong = new java.util.HashMap<>(MONITOR);
        wrong.put("refreshRateHz", "очень быстро");
        assertThrows(IllegalArgumentException.class, () -> new MonitorFactory().create(spec(wrong)));
        assertThrows(IllegalArgumentException.class, () -> new AccessoryFactory().create(null));
    }
}
