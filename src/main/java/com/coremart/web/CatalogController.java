package com.coremart.web;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Category;
import com.coremart.model.Product;
import com.coremart.sorting.SortStrategies;
import com.coremart.sorting.SortStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

/**
 * Catalog page: all products or the products of one category.
 * Data comes from the single {@link ProductCatalog} instance (Singleton); the order is chosen
 * with a {@link SortStrategy} (Strategy) from the {@code sort} parameter.
 */
@Controller
public class CatalogController {

    public static final int LOW_STOCK = 3;

    private final ProductCatalog catalog = ProductCatalog.getInstance();

    @GetMapping({"/", "/catalog"})
    public String catalog(@RequestParam(name = "category", required = false) String category,
                          @RequestParam(name = "sort", required = false) String sort, Model model) {
        Category selected = parseCategory(category);
        SortStrategy strategy = SortStrategies.byCode(sort);
        List<Product> products = strategy.sort(catalog.findByCategory(selected));
        model.addAttribute("categories", Category.values());
        model.addAttribute("selected", selected);
        model.addAttribute("sorts", SortStrategies.all());
        model.addAttribute("sort", strategy);
        model.addAttribute("products", products);
        model.addAttribute("lowStock", LOW_STOCK);
        return "catalog";
    }

    /** Unknown or empty values simply show the whole catalog. */
    static Category parseCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Category.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
