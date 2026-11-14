package com.coremart.bundle;

import com.coremart.builder.ProductBuilder;
import com.coremart.model.Category;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductBundleTest {

    private static Product laptop(int stock) {
        return ProductBuilder.of(Category.LAPTOP).brand("Lenovo").name("IdeaPad Slim 5").price(389_000).stock(stock)
                .cpu("AMD Ryzen 5 7530U").ramGb(16).storageGb(512).screenInches(15.6).weightKg(1.7).build();
    }

    private static Product accessory(String name, long price) {
        return ProductBuilder.of(Category.ACCESSORY).brand("Logitech").name(name).price(price).stock(10)
                .type("Mouse").build();
    }

    @Test
    void leafWrapsOneProduct() {
        Product p = laptop(5);
        ProductItem item = new ProductItem(p);
        assertEquals("Lenovo IdeaPad Slim 5", item.getTitle());
        assertEquals(389_000, item.getPrice());
        assertEquals(p.getFormattedPrice(), item.getFormattedPrice());
        assertTrue(item.isAvailable());
        assertEquals(List.of(p), item.getProducts());
        assertTrue(item.getDetails().isEmpty());
    }

    @Test
    void bundlePriceIsTheSumOfChildrenMinusDiscount() {
        ProductBundle bundle = new ProductBundle("For study", 10)
                .add(laptop(5))
                .add(accessory("M185", 9_000));
        assertEquals(398_000, bundle.getFullPrice());
        assertEquals(358_200, bundle.getPrice());
        assertEquals(39_800, bundle.getSavings());
        assertEquals(2, bundle.getProducts().size());

        ProductBundle odd = new ProductBundle("Rounding", 8).add(accessory("A", 517_000));
        assertEquals(475_600, odd.getPrice(), "475 640 is rounded down to 475 600");
    }

    @Test
    void bundlesCanBeNestedAndAreTreatedLikeAnyOtherItem() {
        ProductBundle peripherals = new ProductBundle("Keyboard and mouse", 5)
                .add(accessory("MX Keys S", 59_000))
                .add(accessory("M185", 9_000));
        ProductBundle setup = new ProductBundle("Setup", 0).add(laptop(3)).add(peripherals);

        StoreItem asItem = setup;
        assertEquals(389_000 + 64_600, asItem.getPrice());
        assertEquals(3, asItem.getProducts().size());
        assertEquals(List.of("Lenovo IdeaPad Slim 5", "Keyboard and mouse",
                "— Logitech MX Keys S", "— Logitech M185"), asItem.getDetails());
    }

    @Test
    void bundleIsAvailableOnlyWhenEveryChildIs() {
        Product laptop = laptop(1);
        ProductBundle bundle = new ProductBundle("Bundle", 5).add(laptop).add(accessory("M185", 9_000));
        assertTrue(bundle.isAvailable());
        laptop.setStock(0);
        assertFalse(bundle.isAvailable());
        assertFalse(new ProductBundle("Empty", 0).isAvailable());
    }

    @Test
    void invalidBundlesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ProductBundle(" ", 5));
        assertThrows(IllegalArgumentException.class, () -> new ProductBundle("Too cheap", 60));
        assertThrows(IllegalArgumentException.class, () -> new ProductBundle("Bundle", -1));

        ProductBundle outer = new ProductBundle("Outer", 0);
        ProductBundle inner = new ProductBundle("Inner", 0).add(outer);
        assertThrows(IllegalArgumentException.class, () -> outer.add(outer));
        assertThrows(IllegalArgumentException.class, () -> outer.add(inner));
        assertThrows(IllegalArgumentException.class, () -> outer.add((StoreItem) null));
    }

    @Test
    void childrenCanBeRemovedButNotChangedFromOutside() {
        ProductItem mouse = new ProductItem(accessory("M185", 9_000));
        ProductBundle bundle = new ProductBundle("Bundle", 0).add(mouse);
        assertThrows(UnsupportedOperationException.class, () -> bundle.getChildren().clear());
        assertTrue(bundle.remove(mouse));
        assertEquals(0, bundle.getPrice());
    }
}
