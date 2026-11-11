package com.coremart.catalog;

import com.coremart.model.Category;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductCatalogTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();

    @Test
    void thereIsOnlyOneCatalog() throws Exception {
        assertSame(catalog, ProductCatalog.getInstance());
        for (Constructor<?> c : ProductCatalog.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()), "constructor must be private");
        }
    }

    @Test
    void allThreadsGetTheSameInstance() throws Exception {
        Set<ProductCatalog> seen = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 50; i++) {
            pool.submit(() -> seen.add(ProductCatalog.getInstance()));
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(1, seen.size());
    }

    @Test
    void catalogIsSeededWithProductsOfEveryCategory() {
        List<Product> all = catalog.findAll();
        assertTrue(all.size() >= 20, "expected about 20 products, got " + all.size());
        for (Category category : Category.values()) {
            List<Product> inCategory = catalog.findByCategory(category);
            assertFalse(inCategory.isEmpty(), "no products in " + category);
            assertTrue(inCategory.stream().allMatch(p -> p.getCategory() == category));
        }
        assertEquals(all.size(), catalog.findByCategory(null).size());
        assertTrue(all.stream().allMatch(p -> p.getPrice() > 0 && p.getStock() >= 1 && p.getStock() <= 25));
    }

    @Test
    void productsHaveUniqueIdsAndTheListCannotBeChanged() {
        List<Product> all = catalog.findAll();
        assertEquals(all.size(), all.stream().map(Product::getId).distinct().count());
        assertThrows(UnsupportedOperationException.class, () -> all.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> catalog.findByCategory(Category.LAPTOP).add(all.get(0)));
        assertEquals(all.size(), catalog.size());
    }

    @Test
    void findById() {
        Product first = catalog.findAll().get(0);
        assertEquals(first, catalog.findById(first.getId()).orElseThrow());
        assertTrue(catalog.findById(-1).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> catalog.getStock(-1));
    }

    @Test
    void stockGoesDownAndBackUp() {
        Product p = catalog.findAll().get(1);
        int before = catalog.getStock(p.getId());
        catalog.decreaseStock(p.getId(), 1);
        assertEquals(before - 1, catalog.getStock(p.getId()));
        catalog.increaseStock(p.getId(), 1);
        assertEquals(before, catalog.getStock(p.getId()));
    }

    @Test
    void stockNeverGoesBelowZero() {
        Product p = catalog.findAll().get(2);
        int before = catalog.getStock(p.getId());
        assertThrows(IllegalStateException.class, () -> catalog.decreaseStock(p.getId(), before + 1));
        assertEquals(before, catalog.getStock(p.getId()));
        assertThrows(IllegalArgumentException.class, () -> catalog.decreaseStock(p.getId(), 0));
        assertThrows(IllegalArgumentException.class, () -> catalog.increaseStock(p.getId(), -3));
    }
}
