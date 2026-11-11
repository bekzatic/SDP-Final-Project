package com.coremart.pricing;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * STRATEGY pattern - maps the promo code a customer types to a <b>Concrete Strategy</b>.
 * Codes are case-insensitive.
 */
public final class PromoCodes {

    private static final Map<String, DiscountStrategy> CODES = Map.of(
            "STUDENT10", new PercentageDiscount(10, 50_000),
            "SALE15", new PercentageDiscount(15, 100_000),
            "CORE5000", new FixedAmountDiscount(5_000, 50_000));

    private PromoCodes() {
    }

    public static Optional<DiscountStrategy> find(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(CODES.get(normalize(code)));
    }

    public static String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
