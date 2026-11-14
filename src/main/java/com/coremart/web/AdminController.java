package com.coremart.web;

import com.coremart.catalog.ProductCatalog;
import com.coremart.checkout.CheckoutFacade;
import com.coremart.model.Product;
import com.coremart.notification.Notification;
import com.coremart.notification.NotificationLog;
import com.coremart.notification.StockAlert;
import com.coremart.order.Order;
import com.coremart.order.OrderAction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Page for the store staff: all orders with the actions their status allows (State),
 * stock levels with restocking, and the warehouse notifications (Observer).
 * Learning project: the page has no login.
 */
@Controller
public class AdminController {

    public static final int MAX_RESTOCK = 100;

    private final ProductCatalog catalog = ProductCatalog.getInstance();
    private final CheckoutFacade checkout;
    private final NotificationLog notifications;

    public AdminController(CheckoutFacade checkout, NotificationLog notifications) {
        this.checkout = checkout;
        this.notifications = notifications;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        List<Order> orders = checkout.allOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("waiting", orders.stream().filter(o -> o.getState().getCode().equals("NEW")).count());
        model.addAttribute("inWork", orders.stream().filter(o -> !o.getStaffActions().isEmpty()
                && !o.getState().getCode().equals("NEW")).count());
        model.addAttribute("revenue", Product.formatTenge(orders.stream()
                .filter(o -> o.getPaymentId() != null && !o.getState().getCode().equals("CANCELLED"))
                .mapToLong(Order::getTotal).sum()));
        model.addAttribute("products", catalog.findAll());
        model.addAttribute("threshold", StockAlert.THRESHOLD);
        model.addAttribute("maxRestock", MAX_RESTOCK);
        model.addAttribute("alerts", notifications.latest(Notification.WAREHOUSE, 10));
        return "admin";
    }

    @PostMapping("/admin/orders/{number}/{action}")
    public String orderAction(@PathVariable("number") String number, @PathVariable("action") String action,
                              RedirectAttributes redirect) {
        Optional<OrderAction> parsed = OrderAction.parse(action);
        try {
            Order order = checkout.advance(number, parsed.orElse(null));
            redirect.addFlashAttribute("message", "Order " + order.getNumber() + ": “" + order.getStatusName() + "”");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/stock")
    public String restock(@RequestParam("productId") long productId,
                          @RequestParam(name = "quantity", defaultValue = "0") int quantity,
                          RedirectAttributes redirect) {
        Optional<Product> product = catalog.findById(productId);
        if (product.isEmpty()) {
            redirect.addFlashAttribute("error", "Product not found");
        } else if (quantity < 1 || quantity > MAX_RESTOCK) {
            redirect.addFlashAttribute("error", "Quantity must be between 1 and " + MAX_RESTOCK);
        } else {
            catalog.increaseStock(productId, quantity);
            redirect.addFlashAttribute("message", "“" + product.get().getFullName() + "”: "
                    + product.get().getStock() + " in stock");
        }
        return "redirect:/admin";
    }
}
