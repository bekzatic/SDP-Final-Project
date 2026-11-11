package com.coremart.web;

import com.coremart.addon.AddOnType;
import com.coremart.bundle.ProductItem;
import com.coremart.bundle.StoreItem;
import com.coremart.cart.Cart;
import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.EnumMap;
import java.util.Map;
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
        addServices(product.get(), model);
        return "product";
    }

    /** Services (Decorator) the customer can add on this product, with their prices. */
    private static void addServices(Product product, Model model) {
        StoreItem item = new ProductItem(product);
        Map<AddOnType, String> prices = new EnumMap<>(AddOnType.class);
        for (AddOnType type : AddOnType.availableFor(item)) {
            prices.put(type, Product.formatTenge(type.priceFor(item)));
        }
        model.addAttribute("warranties", AddOnType.availableFor(item).stream().filter(AddOnType::isWarranty).toList());
        model.addAttribute("extras", AddOnType.availableFor(item).stream().filter(t -> !t.isWarranty()).toList());
        model.addAttribute("addOnPrices", prices);
        model.addAttribute("maxQuantity", Math.min(Cart.MAX_QUANTITY, Math.max(1, product.getStock())));
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
