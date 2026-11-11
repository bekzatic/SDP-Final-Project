package com.coremart.addon;

import com.coremart.builder.ProductBuilder;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;
import com.coremart.bundle.StoreItem;
import com.coremart.model.Category;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddOnDecoratorTest {

    private static final Product LAPTOP = ProductBuilder.of(Category.LAPTOP)
            .brand("Lenovo").name("IdeaPad Slim 5").price(389_000).stock(5)
            .cpu("AMD Ryzen 5 7530U").ramGb(16).storageGb(512).screenInches(15.6).weightKg(1.7).build();
    private static final Product MOUSE = ProductBuilder.of(Category.ACCESSORY)
            .brand("Logitech").name("M185").price(9_000).stock(25).type("Мышь").build();

    @Test
    void decoratorAddsItsPriceAndKeepsTheTitle() {
        StoreItem item = new GiftWrapAddOn(new ProductItem(MOUSE));
        assertEquals("Logitech M185", item.getTitle());
        assertEquals(12_000, item.getPrice());
        assertEquals(List.of(MOUSE), item.getProducts());
        assertTrue(item.isAvailable());
    }

    @Test
    void warrantyCostsTenPercentPerYearWithAMinimum() {
        assertEquals(38_900, new WarrantyAddOn(new ProductItem(LAPTOP), 1).getAddOnPrice());
        assertEquals(77_800, new WarrantyAddOn(new ProductItem(LAPTOP), 2).getAddOnPrice());
        assertEquals(5_000, new WarrantyAddOn(new ProductItem(MOUSE), 1).getAddOnPrice());
        assertEquals("Расширенная гарантия +2 года", new WarrantyAddOn(new ProductItem(LAPTOP), 2).getAddOnName());
        assertThrows(IllegalArgumentException.class, () -> new WarrantyAddOn(new ProductItem(LAPTOP), 0));
        assertThrows(IllegalArgumentException.class, () -> new WarrantyAddOn(new ProductItem(LAPTOP), 4));
    }

    @Test
    void decoratorsCanBeStacked() {
        StoreItem item = new GiftWrapAddOn(new SetupAddOn(new WarrantyAddOn(new ProductItem(LAPTOP), 1)));
        assertEquals(389_000 + 38_900 + 15_000 + 3_000, item.getPrice());
        List<String> details = item.getDetails();
        assertEquals(3, details.size());
        assertTrue(details.get(0).startsWith("+ Расширенная гарантия +1 год"));
        assertTrue(details.get(1).startsWith("+ Установка и настройка ПО"));
        assertTrue(details.get(2).startsWith("+ Подарочная упаковка"));
    }

    @Test
    void decoratorsWorkOnBundlesToo() {
        ProductBundle bundle = new ProductBundle("Для учёбы", 10).add(LAPTOP).add(MOUSE);
        StoreItem wrapped = new SetupAddOn(bundle);
        assertEquals(bundle.getPrice() + SetupAddOn.PRICE, wrapped.getPrice());
        assertEquals("Для учёбы", wrapped.getTitle());
        assertEquals(bundle.getDetails().size() + 1, wrapped.getDetails().size());
    }

    @Test
    void setupNeedsAComputer() {
        assertThrows(IllegalArgumentException.class, () -> new SetupAddOn(new ProductItem(MOUSE)));
        assertThrows(IllegalArgumentException.class, () -> new GiftWrapAddOn(null));
        assertFalse(AddOnType.availableFor(new ProductItem(MOUSE)).contains(AddOnType.SETUP));
        assertTrue(AddOnType.availableFor(new ProductItem(LAPTOP)).contains(AddOnType.SETUP));
    }

    @Test
    void chosenServicesAreAppliedOnce() {
        StoreItem plain = new ProductItem(MOUSE);
        StoreItem item = AddOnType.applyAll(plain,
                List.of("gift_wrap", "WARRANTY_2Y", "WARRANTY_1Y", "SETUP", "UNKNOWN"));
        assertEquals(9_000 + 5_000 + 3_000, item.getPrice());
        GiftWrapAddOn gift = assertInstanceOf(GiftWrapAddOn.class, item);
        WarrantyAddOn warranty = assertInstanceOf(WarrantyAddOn.class, gift.getInner());
        assertEquals(2, warranty.getYears());
        assertSame(plain, warranty.getInner());

        StoreItem laptop = AddOnType.applyAll(new ProductItem(LAPTOP), List.of("GIFT_WRAP", "WARRANTY_1Y"));
        assertEquals(389_000 + 38_900 + 3_000, laptop.getPrice());
        assertSame(plain, AddOnType.applyAll(plain, null));
        assertEquals(38_900, AddOnType.WARRANTY_1Y.priceFor(new ProductItem(LAPTOP)));
    }
}
