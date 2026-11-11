package com.coremart.builder;

import com.coremart.factory.ProductFactory;
import com.coremart.factory.ProductSpec;
import com.coremart.model.Category;
import com.coremart.model.Product;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BUILDER pattern - the <b>Builder</b>.
 * <p>
 * A product has a handful of common fields and, depending on its type, up to six more
 * characteristics. A constructor with a dozen positional arguments is hard to read and easy
 * to get wrong, so products are put together step by step:
 *
 * <pre>
 * Product laptop = ProductBuilder.of(Category.LAPTOP)
 *         .brand("Lenovo").name("IdeaPad Slim 5").price(389_000).stock(8)
 *         .cpu("AMD Ryzen 5 7530U").ramGb(16).storageGb(512).screenInches(15.6).weightKg(1.7)
 *         .build();
 * </pre>
 *
 * Optional characteristics get sensible defaults (for example integrated graphics).
 * {@link #build()} checks that everything required is present and then hands the data to
 * the {@link ProductFactory} of the category, so Builder and Factory Method work together.
 */
public final class ProductBuilder {

    /** Defaults for characteristics that a customer rarely needs to specify. */
    private static final Map<Category, Map<String, Object>> DEFAULTS = Map.of(
            Category.LAPTOP, Map.of("gpu", "Встроенная графика"),
            Category.DESKTOP_PC, Map.of("gpu", "Встроенная графика", "formFactor", "Tower"),
            Category.MONITOR, Map.of("refreshRateHz", 60, "panelType", "IPS"),
            Category.ACCESSORY, Map.of("connection", "USB"));

    private final Category category;
    private final ProductFactory factory;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private String name;
    private String brand;
    private long price;
    private int stock;
    private String description = "";

    private ProductBuilder(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        this.category = category;
        this.factory = ProductFactory.forCategory(category);
    }

    public static ProductBuilder of(Category category) {
        return new ProductBuilder(category);
    }

    // ------------------------------------------------------------ common fields

    public ProductBuilder name(String name) { this.name = name; return this; }
    public ProductBuilder brand(String brand) { this.brand = brand; return this; }
    public ProductBuilder price(long priceTenge) { this.price = priceTenge; return this; }
    public ProductBuilder stock(int stock) { this.stock = stock; return this; }
    public ProductBuilder description(String description) { this.description = description; return this; }

    // ------------------------------------------------------------ characteristics

    public ProductBuilder cpu(String cpu) { return attr("cpu", cpu); }
    public ProductBuilder ramGb(int gb) { return attr("ramGb", gb); }
    public ProductBuilder storageGb(int gb) { return attr("storageGb", gb); }
    public ProductBuilder gpu(String gpu) { return attr("gpu", gpu); }
    public ProductBuilder screenInches(double inches) { return attr("screenInches", inches); }
    public ProductBuilder weightKg(double kg) { return attr("weightKg", kg); }
    public ProductBuilder formFactor(String formFactor) { return attr("formFactor", formFactor); }
    public ProductBuilder sizeInches(double inches) { return attr("sizeInches", inches); }
    public ProductBuilder resolution(String resolution) { return attr("resolution", resolution); }
    public ProductBuilder refreshRateHz(int hz) { return attr("refreshRateHz", hz); }
    public ProductBuilder panelType(String panelType) { return attr("panelType", panelType); }
    public ProductBuilder type(String type) { return attr("type", type); }
    public ProductBuilder connection(String connection) { return attr("connection", connection); }

    /** Rejects characteristics that do not exist for this category, e.g. a refresh rate on a mouse. */
    private ProductBuilder attr(String key, Object value) {
        if (!factory.requiredAttributes().contains(key)) {
            throw new IllegalStateException("'" + key + "' does not apply to "
                    + category.getDisplayName().toLowerCase());
        }
        attributes.put(key, value);
        return this;
    }

    // ------------------------------------------------------------ build

    /** Checks the collected data and creates the product through the category's factory. */
    public Product build() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name is required");
        }
        if (brand == null || brand.isBlank()) {
            throw new IllegalStateException("Brand is required");
        }
        if (price <= 0) {
            throw new IllegalStateException("Price must be set and positive");
        }
        Map<String, Object> all = new LinkedHashMap<>(DEFAULTS.getOrDefault(category, Map.of()));
        all.putAll(attributes);
        for (String key : factory.requiredAttributes()) {
            if (all.get(key) == null) {
                throw new IllegalStateException(category.getDisplayName() + ": '" + key + "' is required");
            }
        }
        return factory.create(new ProductSpec(name, brand, price, stock, description, all));
    }
}
