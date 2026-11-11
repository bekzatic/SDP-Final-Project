package com.coremart.sorting;

import com.coremart.model.Product;

import java.util.Comparator;

/** STRATEGY pattern - a <b>Concrete Strategy</b>: cheapest first. */
public class PriceAscendingSort extends ComparatorSort {

    @Override
    public String getCode() {
        return "price-asc";
    }

    @Override
    public String getLabel() {
        return "Сначала дешевле";
    }

    @Override
    protected Comparator<Product> comparator() {
        return Comparator.comparingLong(Product::getPrice).thenComparing(Product::getFullName);
    }
}
