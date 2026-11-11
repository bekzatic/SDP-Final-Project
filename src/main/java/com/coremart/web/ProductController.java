package com.coremart.web;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * Product page with all characteristics of one product.
 * Data comes from the single {@link ProductCatalog} instance (Singleton).
 */
@Controller
public class ProductController {

    private final ProductCatalog catalog = ProductCatalog.getInstance();

    @GetMapping("/product/{id}")
    public String product(@PathVariable("id") String id, Model model, HttpServletResponse response) {
        Optional<Product> product = parseId(id).flatMap(catalog::findById);
        if (product.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("product", product.get());
        model.addAttribute("lowStock", CatalogController.LOW_STOCK);
        return "product";
    }

    /** Ids that are not numbers are treated like unknown ids. */
    private static Optional<Long> parseId(String id) {
        try {
            return Optional.of(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
