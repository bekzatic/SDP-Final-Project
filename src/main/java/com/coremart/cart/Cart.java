package com.coremart.cart;

import com.coremart.bundle.StoreItem;
import com.coremart.model.Product;
import com.coremart.pricing.DiscountStrategy;
import com.coremart.pricing.NoDiscount;
import com.coremart.pricing.PromoCodes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The shopping cart of one visitor (one per HTTP session).
 * <p>
 * Lines hold {@link StoreItem}s, so the cart treats single products, bundles (Composite) and
 * items with services (Decorator) the same way. The discount is a {@link DiscountStrategy}
 * (Strategy) that changes when a promo code is applied. The cart only checks stock; it is
 * taken from the catalog at checkout.
 */
@Component
@SessionScope
public class Cart {

    public static final int MAX_QUANTITY = 10;

    private final List<CartLine> lines = new ArrayList<>();
    private int nextLineId = 1;
    private DiscountStrategy discount = new NoDiscount();
    private String promoCode;

    // ------------------------------------------------------------ lines

    public CartLine add(StoreItem item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Item is required");
        }
        checkQuantity(quantity);
        if (!item.isAvailable()) {
            throw new IllegalStateException("«" + item.getTitle() + "» нет в наличии");
        }
        checkStock(item, quantity, null);
        CartLine line = new CartLine(nextLineId++, item, quantity);
        lines.add(line);
        return line;
    }

    public void changeQuantity(int lineId, int quantity) {
        CartLine line = findLine(lineId)
                .orElseThrow(() -> new IllegalArgumentException("No cart line " + lineId));
        checkQuantity(quantity);
        checkStock(line.getItem(), quantity, line);
        line.setQuantity(quantity);
    }

    public boolean remove(int lineId) {
        return lines.removeIf(line -> line.getId() == lineId);
    }

    public void clear() {
        lines.clear();
        removePromoCode();
    }

    public Optional<CartLine> findLine(int lineId) {
        return lines.stream().filter(line -> line.getId() == lineId).findFirst();
    }

    public List<CartLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /** Number of items, counting quantities. */
    public int getItemCount() {
        return lines.stream().mapToInt(CartLine::getQuantity).sum();
    }

    /** How many of each catalog product the cart needs, bundles included. */
    public Map<Product, Integer> getRequiredStock() {
        return requiredStock(null);
    }

    private Map<Product, Integer> requiredStock(CartLine skip) {
        Map<Product, Integer> needed = new LinkedHashMap<>();
        for (CartLine line : lines) {
            if (line != skip) {
                for (Product p : line.getItem().getProducts()) {
                    needed.merge(p, line.getQuantity(), Integer::sum);
                }
            }
        }
        return needed;
    }

    private void checkStock(StoreItem item, int quantity, CartLine skip) {
        Map<Product, Integer> needed = requiredStock(skip);
        for (Product p : item.getProducts()) {
            needed.merge(p, quantity, Integer::sum);
        }
        for (Map.Entry<Product, Integer> e : needed.entrySet()) {
            if (e.getValue() > e.getKey().getStock()) {
                throw new IllegalStateException("«" + e.getKey().getFullName() + "»: в наличии только "
                        + e.getKey().getStock() + " шт.");
            }
        }
    }

    private static void checkQuantity(int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Количество должно быть от 1 до " + MAX_QUANTITY);
        }
    }

    // ------------------------------------------------------------ discount

    /** Applies a promo code; returns false and keeps the current discount if the code is unknown. */
    public boolean applyPromoCode(String code) {
        Optional<DiscountStrategy> strategy = PromoCodes.find(code);
        if (strategy.isEmpty()) {
            return false;
        }
        discount = strategy.get();
        promoCode = PromoCodes.normalize(code);
        return true;
    }

    public void removePromoCode() {
        discount = new NoDiscount();
        promoCode = null;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public DiscountStrategy getDiscountStrategy() {
        return discount;
    }

    // ------------------------------------------------------------ totals

    public long getSubtotal() {
        return lines.stream().mapToLong(CartLine::getTotal).sum();
    }

    public long getDiscount() {
        long subtotal = getSubtotal();
        return Math.max(0, Math.min(subtotal, discount.discountFor(subtotal)));
    }

    public long getTotal() {
        return getSubtotal() - getDiscount();
    }

    public String getFormattedSubtotal() {
        return Product.formatTenge(getSubtotal());
    }

    public String getFormattedDiscount() {
        return Product.formatTenge(getDiscount());
    }

    public String getFormattedTotal() {
        return Product.formatTenge(getTotal());
    }
}
