package com.coremart.web;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Category;
import com.coremart.model.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

/**
 * Catalog page: all products or the products of one category.
 * Data comes from the single {@link ProductCatalog} instance (Singleton).
 */
@Controller
public class CatalogController {

    /** Products with this many items or fewer are marked as "low stock". */
    public static final int LOW_STOCK = 3;

    private final ProductCatalog catalog = ProductCatalog.getInstance();

    @GetMapping({"/", "/catalog"})
    public String catalog(@RequestParam(name = "category", required = false) String category, Model model) {
        Category selected = parseCategory(category);
        List<Product> products = catalog.findByCategory(selected);
        model.addAttribute("categories", Category.values());
        model.addAttribute("selected", selected);
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
