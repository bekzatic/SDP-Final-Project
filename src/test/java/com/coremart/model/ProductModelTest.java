package com.coremart.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductModelTest {

    private final Laptop laptop = new Laptop(1, "IdeaPad Slim 5", "Lenovo", 389_000, 8, "Тонкий ноутбук",
            "AMD Ryzen 5 7530U", 16, 512, "AMD Radeon Graphics", 15.6, 1.7);

    @Test
    void eachSubclassReportsItsCategory() {
        assertEquals(Category.LAPTOP, laptop.getCategory());
        assertEquals(Category.DESKTOP_PC, new DesktopPC(2, "Pavilion", "HP", 299_000, 3, "",
                "Intel Core i5-12400", 16, 512, "Intel UHD 730", "Tower").getCategory());
        assertEquals(Category.MONITOR, new Monitor(3, "U2723QE", "Dell", 249_000, 5, "",
                27, "3840 × 2160", 60, "IPS").getCategory());
        assertEquals(Category.ACCESSORY, new Accessory(4, "MX Master 3S", "Logitech", 49_000, 20, "",
                "Мышь", "Bluetooth, USB-приёмник").getCategory());
    }

    @Test
    void specsKeepTheirDisplayOrder() {
        assertEquals(List.of("Процессор", "Оперативная память", "Накопитель", "Видеокарта", "Экран", "Вес"),
                List.copyOf(laptop.getSpecs().keySet()));
        assertEquals("512 ГБ SSD", laptop.getSpecs().get("Накопитель"));
        assertEquals("15,6″", laptop.getSpecs().get("Экран"));
        assertEquals("1,7 кг", laptop.getSpecs().get("Вес"));
    }

    @Test
    void terabyteStorageIsShownInTerabytes() {
        DesktopPC pc = new DesktopPC(5, "Legion Tower 5", "Lenovo", 899_000, 2, "",
                "AMD Ryzen 7 7700", 32, 1000, "NVIDIA GeForce RTX 4070", "Tower");
        assertEquals("1 ТБ SSD", pc.getSpecs().get("Накопитель"));
    }

    @Test
    void priceIsFormattedInTenge() {
        assertEquals("389 000 ₸", laptop.getFormattedPrice());
        assertEquals("1 249 990 ₸", Product.formatTenge(1_249_990));
        assertEquals("Lenovo IdeaPad Slim 5", laptop.getFullName());
    }

    @Test
    void invalidValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Accessory(6, " ", "Razer", 10_000, 1, "", "Мышь", "USB"));
        assertThrows(IllegalArgumentException.class, () -> new Accessory(6, "Viper", "Razer", 0, 1, "", "Мышь", "USB"));
        assertThrows(IllegalArgumentException.class, () -> new Accessory(6, "Viper", "Razer", 10_000, -1, "", "Мышь", "USB"));
        assertThrows(IllegalArgumentException.class, () -> laptop.setStock(-2));
    }

    @Test
    void stockStatusFollowsStock() {
        assertTrue(laptop.isInStock());
        laptop.setStock(0);
        assertFalse(laptop.isInStock());
    }
}
