package com.coremart.catalog;

import com.coremart.builder.ProductBuilder;
import com.coremart.model.Category;
import com.coremart.model.Product;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SINGLETON pattern - the <b>Singleton</b>.
 * <p>
 * The store has exactly one catalog: every page, test and the cart must see the same
 * products and the same stock levels. The constructor is private and the only instance is
 * created lazily by the <i>initialization-on-demand holder</i> idiom: the JVM loads
 * {@link Holder} on the first call to {@link #getInstance()}, and class loading is
 * thread-safe, so no locks or {@code volatile} fields are needed.
 * <p>
 * The catalog is filled with products created through {@link ProductBuilder}.
 */
public final class ProductCatalog {

    private final Map<Long, Product> products = new LinkedHashMap<>();

    private ProductCatalog() {
        seed();
    }

    private static final class Holder {
        private static final ProductCatalog INSTANCE = new ProductCatalog();
    }

    public static ProductCatalog getInstance() {
        return Holder.INSTANCE;
    }

    public List<Product> findAll() {
        synchronized (products) {
            return List.copyOf(products.values());
        }
    }

    public Optional<Product> findById(long id) {
        synchronized (products) {
            return Optional.ofNullable(products.get(id));
        }
    }

    public List<Product> findByCategory(Category category) {
        if (category == null) {
            return findAll();
        }
        synchronized (products) {
            return products.values().stream().filter(p -> p.getCategory() == category).toList();
        }
    }

    public int getStock(long id) {
        return require(id).getStock();
    }

    public int size() {
        synchronized (products) {
            return products.size();
        }
    }

    public void decreaseStock(long id, int quantity) {
        checkQuantity(quantity);
        synchronized (products) {
            Product p = require(id);
            if (p.getStock() < quantity) {
                throw new IllegalStateException("Not enough '" + p.getFullName() + "' in stock: "
                        + p.getStock() + " left, " + quantity + " requested");
            }
            p.setStock(p.getStock() - quantity);
        }
    }

    public void increaseStock(long id, int quantity) {
        checkQuantity(quantity);
        synchronized (products) {
            Product p = require(id);
            p.setStock(p.getStock() + quantity);
        }
    }

    private Product require(long id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Unknown product id " + id));
    }

    private static void checkQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    private void add(Product p) {
        products.put(p.getId(), p);
    }

    private void seed() {
        // Laptops
        add(ProductBuilder.of(Category.LAPTOP).brand("Lenovo").name("IdeaPad Slim 5").price(389_000).stock(8)
                .cpu("AMD Ryzen 5 7530U").ramGb(16).storageGb(512).gpu("AMD Radeon Graphics")
                .screenInches(15.6).weightKg(1.7)
                .description("Lightweight laptop for study and work with a bright IPS display.").build());
        add(ProductBuilder.of(Category.LAPTOP).brand("ASUS").name("Vivobook 15 X1504").price(279_000).stock(12)
                .cpu("Intel Core i5-1235U").ramGb(8).storageGb(512).gpu("Intel Iris Xe")
                .screenInches(15.6).weightKg(1.7)
                .description("Affordable everyday laptop.").build());
        add(ProductBuilder.of(Category.LAPTOP).brand("Apple").name("MacBook Air 13 M3").price(649_000).stock(5)
                .cpu("Apple M3").ramGb(16).storageGb(512).gpu("Apple M3 10-core GPU")
                .screenInches(13.6).weightKg(1.24)
                .description("Thin, silent fanless laptop with up to 18 hours of battery life.").build());
        add(ProductBuilder.of(Category.LAPTOP).brand("ASUS").name("ROG Strix G16").price(899_000).stock(3)
                .cpu("Intel Core i7-13650HX").ramGb(16).storageGb(1000).gpu("NVIDIA GeForce RTX 4060")
                .screenInches(16).weightKg(2.5)
                .description("Gaming laptop with a 165 Hz display.").build());
        add(ProductBuilder.of(Category.LAPTOP).brand("HP").name("Victus 15").price(459_000).stock(7)
                .cpu("AMD Ryzen 5 7535HS").ramGb(16).storageGb(512).gpu("NVIDIA GeForce RTX 2050")
                .screenInches(15.6).weightKg(2.3)
                .description("Entry-level gaming laptop.").build());
        add(ProductBuilder.of(Category.LAPTOP).brand("Acer").name("Swift Go 14").price(499_000).stock(2)
                .cpu("Intel Core Ultra 5 125H").ramGb(16).storageGb(512).gpu("Intel Arc Graphics")
                .screenInches(14).weightKg(1.3)
                .description("Compact ultrabook with an OLED display.").build());

        // Desktop PCs
        add(ProductBuilder.of(Category.DESKTOP_PC).brand("HP").name("Pavilion TP01").price(299_000).stock(6)
                .cpu("Intel Core i5-12400").ramGb(16).storageGb(512)
                .description("Quiet computer for home and office.").build());
        add(ProductBuilder.of(Category.DESKTOP_PC).brand("Lenovo").name("Legion Tower 5").price(749_000).stock(4)
                .cpu("AMD Ryzen 7 7700").ramGb(32).storageGb(1000).gpu("NVIDIA GeForce RTX 4060 Ti")
                .formFactor("Mid Tower")
                .description("Gaming PC for modern games at 1440p.").build());
        add(ProductBuilder.of(Category.DESKTOP_PC).brand("MSI").name("MAG Infinite S3").price(569_000).stock(3)
                .cpu("Intel Core i5-14400F").ramGb(16).storageGb(1000).gpu("NVIDIA GeForce RTX 4060")
                .formFactor("Mid Tower")
                .description("Ready-to-play gaming tower.").build());
        add(ProductBuilder.of(Category.DESKTOP_PC).brand("Apple").name("Mac mini M4").price(359_000).stock(9)
                .cpu("Apple M4").ramGb(16).storageGb(256).gpu("Apple M4 10-core GPU").formFactor("Mini PC")
                .description("Small but powerful computer.").build());
        add(ProductBuilder.of(Category.DESKTOP_PC).brand("Dell").name("OptiPlex 7010 SFF").price(339_000).stock(10)
                .cpu("Intel Core i5-13500").ramGb(16).storageGb(512).formFactor("Small Form Factor")
                .description("Reliable office computer.").build());

        // Monitors
        add(ProductBuilder.of(Category.MONITOR).brand("Dell").name("P2423").price(119_000).stock(11)
                .sizeInches(24).resolution("1920 × 1200")
                .description("Office monitor with height adjustment.").build());
        add(ProductBuilder.of(Category.MONITOR).brand("Samsung").name("Odyssey G5 27").price(159_000).stock(6)
                .sizeInches(27).resolution("2560 × 1440").refreshRateHz(165).panelType("VA")
                .description("Curved gaming monitor.").build());
        add(ProductBuilder.of(Category.MONITOR).brand("LG").name("UltraGear 27GP850").price(219_000).stock(4)
                .sizeInches(27).resolution("2560 × 1440").refreshRateHz(180).panelType("Nano IPS")
                .description("Fast IPS panel for esports.").build());
        add(ProductBuilder.of(Category.MONITOR).brand("Xiaomi").name("Mi Desktop Monitor 27").price(69_000).stock(15)
                .sizeInches(27).resolution("1920 × 1080").refreshRateHz(75)
                .description("Simple monitor for home.").build());
        add(ProductBuilder.of(Category.MONITOR).brand("ASUS").name("ProArt PA279CV").price(239_000).stock(1)
                .sizeInches(27).resolution("3840 × 2160")
                .description("4K monitor with accurate colors for designers.").build());

        // Accessories
        add(ProductBuilder.of(Category.ACCESSORY).brand("Logitech").name("M185").price(9_000).stock(25)
                .type("Mouse").connection("Wireless (USB receiver)")
                .description("Compact wireless mouse.").build());
        add(ProductBuilder.of(Category.ACCESSORY).brand("Logitech").name("MX Keys S").price(59_000).stock(5)
                .type("Keyboard").connection("Bluetooth")
                .description("Quiet backlit keyboard.").build());
        add(ProductBuilder.of(Category.ACCESSORY).brand("HyperX").name("Cloud II").price(45_000).stock(8)
                .type("Headset").connection("USB / 3.5 mm")
                .description("Gaming headset with surround sound.").build());
        add(ProductBuilder.of(Category.ACCESSORY).brand("Samsung").name("T7 1 TB").price(55_000).stock(3)
                .type("External SSD").connection("USB-C")
                .description("Fast portable drive.").build());
        add(ProductBuilder.of(Category.ACCESSORY).brand("Logitech").name("C920 HD Pro").price(39_000).stock(7)
                .type("Webcam")
                .description("Full HD camera for video calls.").build());
    }
}
