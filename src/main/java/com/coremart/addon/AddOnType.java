package com.coremart.addon;

import com.coremart.bundle.StoreItem;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The services a customer can tick on the product page. Each value knows how to wrap an item
 * in its decorator, so the web layer does not need to know the decorator classes.
 */
public enum AddOnType {

    WARRANTY_1Y("Гарантия +1 год") {
        @Override
        public StoreItem wrap(StoreItem item) { return new WarrantyAddOn(item, 1); }
    },
    WARRANTY_2Y("Гарантия +2 года") {
        @Override
        public StoreItem wrap(StoreItem item) { return new WarrantyAddOn(item, 2); }
    },
    GIFT_WRAP("Подарочная упаковка") {
        @Override
        public StoreItem wrap(StoreItem item) { return new GiftWrapAddOn(item); }
    },
    SETUP("Установка и настройка ПО") {
        @Override
        public StoreItem wrap(StoreItem item) { return new SetupAddOn(item); }

        @Override
        public boolean appliesTo(StoreItem item) { return SetupAddOn.containsComputer(item); }
    };

    private final String label;

    AddOnType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Wraps the item in this service's decorator. */
    public abstract StoreItem wrap(StoreItem item);

    public boolean appliesTo(StoreItem item) {
        return true;
    }

    /** Price of this service for the given item. */
    public long priceFor(StoreItem item) {
        return ((AddOnDecorator) wrap(item)).getAddOnPrice();
    }

    public boolean isWarranty() {
        return this == WARRANTY_1Y || this == WARRANTY_2Y;
    }

    public static List<AddOnType> availableFor(StoreItem item) {
        return Arrays.stream(values()).filter(t -> t.appliesTo(item)).toList();
    }

    public static Optional<AddOnType> parse(String value) {
        return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(value)).findFirst();
    }

    /**
     * Wraps the item in every chosen service, in declaration order so the warranty is priced
     * on the goods alone. Unknown or unsuitable codes are skipped and only the longest
     * warranty is used.
     */
    public static StoreItem applyAll(StoreItem item, List<String> codes) {
        List<AddOnType> chosen = (codes == null ? List.<String>of() : codes).stream()
                .map(AddOnType::parse).flatMap(Optional::stream).distinct().toList();
        AddOnType warranty = chosen.contains(WARRANTY_2Y) ? WARRANTY_2Y
                : chosen.contains(WARRANTY_1Y) ? WARRANTY_1Y : null;
        StoreItem result = item;
        for (AddOnType type : values()) {
            boolean wanted = type.isWarranty() ? type == warranty : chosen.contains(type);
            if (wanted && type.appliesTo(item)) {
                result = type.wrap(result);
            }
        }
        return result;
    }
}
