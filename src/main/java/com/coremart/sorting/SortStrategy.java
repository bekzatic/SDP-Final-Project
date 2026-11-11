package com.coremart.sorting;

import com.coremart.model.Product;

import java.util.List;

/**
 * STRATEGY pattern - the <b>Strategy</b> interface for catalog sorting.
 * <p>
 * The catalog page does not know how products are ordered: it asks {@link SortStrategies}
 * for the strategy chosen in the drop-down and lets it sort the list. A new order is a new
 * class, with no {@code if/else} in the controller.
 */
public interface SortStrategy {

    /** Value used in the URL, e.g. {@code price-asc}. */
    String getCode();

    /** Text shown in the drop-down. */
    String getLabel();

    /** Returns a new sorted list; the given list is not changed. */
    List<Product> sort(List<Product> products);
}
