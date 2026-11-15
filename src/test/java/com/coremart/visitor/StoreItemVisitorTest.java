package com.coremart.visitor;

import com.coremart.addon.AddOnDecorator;
import com.coremart.addon.GiftWrapAddOn;
import com.coremart.addon.WarrantyAddOn;
import com.coremart.builder.ProductBuilder;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;
import com.coremart.bundle.StoreItem;
import com.coremart.model.Category;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoreItemVisitorTest {

    private static final Product LAPTOP = ProductBuilder.of(Category.LAPTOP).brand("Lenovo").name("IdeaPad Slim 5")
            .price(389_000).stock(5).cpu("AMD Ryzen 5 7530U").ramGb(16).storageGb(512)
            .screenInches(15.6).weightKg(1.7).build();
    private static final Product MOUSE = ProductBuilder.of(Category.ACCESSORY).brand("Logitech").name("M185")
            .price(9_000).stock(10).type("Mouse").build();

    @Test
    void everyItemCallsTheMethodForItsOwnType() {
        List<String> calls = new ArrayList<>();
        StoreItemVisitor<Void> recorder = new StoreItemVisitor<>() {
            public Void visitProduct(ProductItem item) { calls.add("product"); return null; }
            public Void visitBundle(ProductBundle bundle) { calls.add("bundle"); return null; }
            public Void visitAddOn(AddOnDecorator addOn) { calls.add("add-on"); return null; }
        };
        new ProductItem(MOUSE).accept(recorder);
        new ProductBundle("Kit", 5).add(LAPTOP).accept(recorder);
        new GiftWrapAddOn(new ProductItem(MOUSE)).accept(recorder);
        assertEquals(List.of("product", "bundle", "add-on"), calls);
    }

    @Test
    void detailsVisitorListsBundleContentsAndServices() {
        StoreItem item = new GiftWrapAddOn(new ProductBundle("Kit", 5).add(LAPTOP).add(MOUSE));
        assertEquals(List.of("Lenovo IdeaPad Slim 5", "Logitech M185", "+ Gift wrapping (3,000 ₸)"),
                item.accept(new DetailsVisitor()));
        assertTrue(new ProductItem(MOUSE).accept(new DetailsVisitor()).isEmpty());
    }

    @Test
    void contentsVisitorFindsTheProductsBehindServices() {
        StoreItem item = new WarrantyAddOn(new ProductBundle("Kit", 5).add(LAPTOP).add(MOUSE), 1);
        assertEquals(List.of(LAPTOP, MOUSE), item.accept(new ContentsVisitor()));
    }

    @Test
    void computerCheckVisitor() {
        ComputerCheckVisitor check = new ComputerCheckVisitor();
        assertTrue(new ProductItem(LAPTOP).accept(check));
        assertFalse(new ProductItem(MOUSE).accept(check));
        assertTrue(new ProductBundle("Kit", 5).add(MOUSE).add(LAPTOP).accept(check));
        assertFalse(new GiftWrapAddOn(new ProductItem(MOUSE)).accept(check));
    }
}
