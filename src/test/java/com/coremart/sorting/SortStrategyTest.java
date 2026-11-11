package com.coremart.sorting;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortStrategyTest {

    private final List<Product> products = ProductCatalog.getInstance().findAll();

    @Test
    void priceAscendingPutsTheCheapestFirst() {
        List<Product> sorted = new PriceAscendingSort().sort(products);
        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(sorted.get(i - 1).getPrice() <= sorted.get(i).getPrice());
        }
    }

    @Test
    void priceDescendingPutsTheMostExpensiveFirst() {
        List<Product> sorted = new PriceDescendingSort().sort(products);
        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(sorted.get(i - 1).getPrice() >= sorted.get(i).getPrice());
        }
    }

    @Test
    void nameAndStockOrders() {
        List<Product> byName = new NameSort().sort(products);
        for (int i = 1; i < byName.size(); i++) {
            assertTrue(byName.get(i - 1).getFullName().compareToIgnoreCase(byName.get(i).getFullName()) <= 0);
        }
        List<Product> byStock = new StockSort().sort(products);
        for (int i = 1; i < byStock.size(); i++) {
            assertTrue(byStock.get(i - 1).getStock() >= byStock.get(i).getStock());
        }
    }

    @Test
    void defaultKeepsTheOrderAndNoStrategyChangesTheInput() {
        assertEquals(products, new DefaultSort().sort(products));
        List<Product> before = List.copyOf(products);
        for (SortStrategy s : SortStrategies.all()) {
            assertEquals(products.size(), s.sort(products).size(), s.getCode());
        }
        assertEquals(before, products);
    }

    @Test
    void strategyIsChosenByCode() {
        assertInstanceOf(PriceAscendingSort.class, SortStrategies.byCode("price-asc"));
        assertInstanceOf(PriceDescendingSort.class, SortStrategies.byCode(" PRICE-DESC "));
        assertInstanceOf(DefaultSort.class, SortStrategies.byCode("random"));
        assertInstanceOf(DefaultSort.class, SortStrategies.byCode(null));
        assertEquals(5, SortStrategies.all().size());
    }
}
