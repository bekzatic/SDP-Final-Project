package com.coremart.sorting;

import com.coremart.model.Product;

import java.util.Comparator;

/** STRATEGY pattern - a <b>Concrete Strategy</b>: by brand and model, A to Z. */
public class NameSort extends ComparatorSort {

    @Override
    public String getCode() {
        return "name";
    }

    @Override
    public String getLabel() {
        return "Name";
    }

    @Override
    protected Comparator<Product> comparator() {
        return Comparator.comparing(Product::getFullName, String.CASE_INSENSITIVE_ORDER);
    }
}
