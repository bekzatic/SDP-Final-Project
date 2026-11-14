package com.coremart.builder;

import com.coremart.model.Accessory;
import com.coremart.model.Category;
import com.coremart.model.DesktopPC;
import com.coremart.model.Laptop;
import com.coremart.model.Monitor;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductBuilderTest {

    private static ProductBuilder laptop() {
        return ProductBuilder.of(Category.LAPTOP)
                .brand("Lenovo").name("IdeaPad Slim 5").price(389_000).stock(8)
                .cpu("AMD Ryzen 5 7530U").ramGb(16).storageGb(512).screenInches(15.6).weightKg(1.7);
    }

    @Test
    void buildsAProductStepByStep() {
        Product p = laptop().gpu("AMD Radeon Graphics").description("Light laptop for study").build();
        Laptop l = assertInstanceOf(Laptop.class, p);
        assertEquals("Lenovo IdeaPad Slim 5", l.getFullName());
        assertEquals(389_000, l.getPrice());
        assertEquals(8, l.getStock());
        assertEquals(16, l.getRamGb());
        assertEquals("AMD Radeon Graphics", l.getGpu());
        assertEquals("Light laptop for study", l.getDescription());
    }

    @Test
    void optionalCharacteristicsGetDefaults() {
        assertEquals("Integrated graphics", ((Laptop) laptop().build()).getGpu());

        DesktopPC pc = (DesktopPC) ProductBuilder.of(Category.DESKTOP_PC)
                .brand("HP").name("Pavilion TP01").price(299_000)
                .cpu("Intel Core i5-12400").ramGb(16).storageGb(512).build();
        assertEquals("Tower", pc.getFormFactor());
        assertEquals(0, pc.getStock());

        Monitor m = (Monitor) ProductBuilder.of(Category.MONITOR)
                .brand("Dell").name("P2423").price(119_000).sizeInches(24).resolution("1920 × 1200").build();
        assertEquals(60, m.getRefreshRateHz());
        assertEquals("IPS", m.getPanelType());
    }

    @Test
    void accessoryNeedsOnlyItsType() {
        Product mouse = ProductBuilder.of(Category.ACCESSORY)
                .brand("Logitech").name("M185").price(9_000).stock(25).type("Mouse").build();
        assertInstanceOf(Accessory.class, mouse);
        assertEquals("USB", mouse.getSpecs().get("Connection"));
    }

    @Test
    void requiredCommonFieldsAreChecked() {
        assertThrows(IllegalStateException.class, () -> laptop().name(null).build());
        assertThrows(IllegalStateException.class, () -> laptop().brand(" ").build());
        assertThrows(IllegalStateException.class, () -> laptop().price(0).build());
    }

    @Test
    void requiredCharacteristicsAreChecked() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> ProductBuilder.of(Category.LAPTOP)
                .brand("ASUS").name("Vivobook 15").price(279_000).ramGb(8).storageGb(512)
                .screenInches(15.6).weightKg(1.7).build());
        assertTrue(e.getMessage().contains("cpu"), e.getMessage());
    }

    @Test
    void builderHandsTheProductToTheFactory() {
        ProductBuilder builder = laptop();
        Product first = builder.build();
        Product second = builder.build();
        assertInstanceOf(Laptop.class, second);
        assertTrue(second.getId() > first.getId(), "each build() goes through the factory and gets a new id");
        assertEquals(first.getFullName(), second.getFullName());
    }

    @Test
    void explicitValuesOverrideDefaults() {
        Monitor m = (Monitor) ProductBuilder.of(Category.MONITOR)
                .brand("Samsung").name("Odyssey G5").price(159_000)
                .sizeInches(27).resolution("2560 × 1440").refreshRateHz(165).panelType("VA").build();
        assertEquals(165, m.getRefreshRateHz());
        assertEquals("VA", m.getPanelType());
    }

    @Test
    void characteristicsOfAnotherCategoryAreRejected() {
        assertThrows(IllegalStateException.class, () -> ProductBuilder.of(Category.ACCESSORY).refreshRateHz(144));
        assertThrows(IllegalStateException.class, () -> ProductBuilder.of(Category.MONITOR).cpu("Intel Core i9"));
        assertThrows(IllegalArgumentException.class, () -> ProductBuilder.of(null));
    }
}
