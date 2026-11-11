package com.coremart.sorting;

import java.util.List;

/**
 * STRATEGY pattern - picks the <b>Concrete Strategy</b> for the code from the URL.
 * Unknown codes fall back to {@link DefaultSort}.
 */
public final class SortStrategies {

    private static final List<SortStrategy> ALL = List.of(
            new DefaultSort(), new PriceAscendingSort(), new PriceDescendingSort(), new NameSort(), new StockSort());

    private SortStrategies() {
    }

    public static List<SortStrategy> all() {
        return ALL;
    }

    public static SortStrategy byCode(String code) {
        return ALL.stream().filter(s -> s.getCode().equalsIgnoreCase(code == null ? "" : code.trim()))
                .findFirst().orElse(ALL.get(0));
    }
}
