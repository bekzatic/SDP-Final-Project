package com.coremart.sorting;

import com.coremart.model.Product;

import java.util.Comparator;
import java.util.List;

/** Shared base for strategies that sort with a comparator. */
abstract class ComparatorSort implements SortStrategy {

    protected abstract Comparator<Product> comparator();

    @Override
    public List<Product> sort(List<Product> products) {
        return products.stream().sorted(comparator()).toList();
    }
}
