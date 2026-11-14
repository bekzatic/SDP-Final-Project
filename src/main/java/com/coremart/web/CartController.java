package com.coremart.web;

import com.coremart.addon.AddOnType;
import com.coremart.bundle.BundleCatalog;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;
import com.coremart.bundle.StoreItem;
import com.coremart.cart.Cart;
import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/** Cart page and the actions on it: add, change quantity, remove, promo code. */
@Controller
public class CartController {

    private final ProductCatalog catalog = ProductCatalog.getInstance();
    private final Cart cart;
    private final BundleCatalog bundles;

    public CartController(Cart cart, BundleCatalog bundles) {
        this.cart = cart;
        this.bundles = bundles;
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("cart", cart);
        return "cart";
    }

    /** Adds a product with the services ticked on the product page (Decorator). */
    @PostMapping("/cart/add")
    public String add(@RequestParam("productId") long productId,
                      @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                      @RequestParam(name = "addOns", required = false) List<String> addOns,
                      RedirectAttributes redirect) {
        Optional<Product> product = catalog.findById(productId);
        if (product.isEmpty()) {
            redirect.addFlashAttribute("error", "Product not found");
            return "redirect:/catalog";
        }
        StoreItem item = AddOnType.applyAll(new ProductItem(product.get()), addOns);
        return addToCart(item, quantity, "/product/" + productId, redirect);
    }

    /** Adds a whole bundle; the cart treats it like any other item (Composite). */
    @PostMapping("/cart/add-bundle")
    public String addBundle(@RequestParam("bundleId") String bundleId,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            RedirectAttributes redirect) {
        Optional<ProductBundle> bundle = bundles.findById(bundleId);
        if (bundle.isEmpty()) {
            redirect.addFlashAttribute("error", "Bundle not found");
            return "redirect:/bundles";
        }
        return addToCart(bundle.get(), quantity, "/bundles", redirect);
    }

    private String addToCart(StoreItem item, int quantity, String backUrl, RedirectAttributes redirect) {
        try {
            cart.add(item, quantity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:" + backUrl;
        }
        redirect.addFlashAttribute("message", "“" + item.getTitle() + "” added to cart");
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String update(@RequestParam("lineId") int lineId, @RequestParam("quantity") int quantity,
                         RedirectAttributes redirect) {
        try {
            cart.changeQuantity(lineId, quantity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam("lineId") int lineId) {
        cart.remove(lineId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clear() {
        cart.clear();
        return "redirect:/cart";
    }

    /** Swaps the cart's discount strategy for the one behind the promo code (Strategy). */
    @PostMapping("/cart/promo")
    public String promo(@RequestParam(name = "code", defaultValue = "") String code, RedirectAttributes redirect) {
        if (cart.applyPromoCode(code)) {
            redirect.addFlashAttribute("message", "Promo code " + cart.getPromoCode() + " applied");
        } else {
            redirect.addFlashAttribute("error", "Promo code “" + code.trim() + "” not found");
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/promo/remove")
    public String removePromo() {
        cart.removePromoCode();
        return "redirect:/cart";
    }
}
