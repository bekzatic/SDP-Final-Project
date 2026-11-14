package com.coremart.sorting;

import com.coremart.model.Product;

import java.util.List;

/** STRATEGY pattern - a <b>Concrete Strategy</b>: keeps the catalog order. */
public class DefaultSort implements SortStrategy {

    @Override
    public String getCode() {
        return "default";
    }

    @Override
    public String getLabel() {
        return "Default";
    }

    @Override
    public List<Product> sort(List<Product> products) {
        return List.copyOf(products);
    }
}
