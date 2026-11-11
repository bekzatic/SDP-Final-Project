package com.coremart.pricing;

/**
 * STRATEGY pattern - the <b>Strategy</b> interface for discounts.
 * <p>
 * The cart keeps one discount strategy, by default {@link NoDiscount}. Entering a promo code
 * swaps in another strategy from {@link PromoCodes}; the cart only calls
 * {@link #discountFor(long)} and never knows how the discount is calculated.
 */
public interface DiscountStrategy {

    /** Short text for the cart, e.g. "Скидка 10%, не больше 50 000 ₸". */
    String getDescription();

    /** Discount in tenge for the given subtotal; never negative and never above the subtotal. */
    long discountFor(long subtotal);
}
