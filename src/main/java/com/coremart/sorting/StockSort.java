package com.coremart.sorting;

import com.coremart.model.Product;

import java.util.Comparator;

/** STRATEGY pattern - a <b>Concrete Strategy</b>: products with the most items in stock first. */
public class StockSort extends ComparatorSort {

    @Override
    public String getCode() {
        return "stock";
    }

    @Override
    public String getLabel() {
        return "Most in stock";
    }

    @Override
    protected Comparator<Product> comparator() {
        return Comparator.comparingInt(Product::getStock).reversed().thenComparing(Product::getFullName);
    }
}
