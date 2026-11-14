package com.coremart.sorting;

import com.coremart.model.Product;

import java.util.Comparator;

/** STRATEGY pattern - a <b>Concrete Strategy</b>: most expensive first. */
public class PriceDescendingSort extends ComparatorSort {

    @Override
    public String getCode() {
        return "price-desc";
    }

    @Override
    public String getLabel() {
        return "Price: high to low";
    }

    @Override
    protected Comparator<Product> comparator() {
        return Comparator.comparingLong(Product::getPrice).reversed().thenComparing(Product::getFullName);
    }
}
