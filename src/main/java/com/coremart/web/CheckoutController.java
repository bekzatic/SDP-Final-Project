package com.coremart.web;

import com.coremart.cart.Cart;
import com.coremart.checkout.CheckoutException;
import com.coremart.checkout.CheckoutFacade;
import com.coremart.checkout.CheckoutForm;
import com.coremart.notification.Notification;
import com.coremart.notification.NotificationLog;
import com.coremart.order.DeliveryMethod;
import com.coremart.order.Order;
import com.coremart.order.PaymentMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Checkout page and the customer's orders. Every action is one call to the
 * {@link CheckoutFacade} (Facade); the page does not touch stock, payments or notifications.
 */
@Controller
public class CheckoutController {

    private final Cart cart;
    private final CheckoutFacade checkout;
    private final CustomerOrders myOrders;
    private final NotificationLog notifications;

    public CheckoutController(Cart cart, CheckoutFacade checkout, CustomerOrders myOrders,
                              NotificationLog notifications) {
        this.cart = cart;
        this.checkout = checkout;
        this.myOrders = myOrders;
        this.notifications = notifications;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, RedirectAttributes redirect) {
        if (cart.isEmpty()) {
            redirect.addFlashAttribute("error", "Your cart is empty: add some products first");
            return "redirect:/cart";
        }
        return showForm(model, new CheckoutForm(), List.of());
    }

    @PostMapping("/checkout")
    public String placeOrder(@ModelAttribute("form") CheckoutForm form, BindingResult binding, Model model,
                             RedirectAttributes redirect) {
        if (cart.isEmpty()) {
            redirect.addFlashAttribute("error", "Your cart is empty: add some products first");
            return "redirect:/cart";
        }
        try {
            Order order = checkout.placeOrder(cart, form);
            myOrders.add(order.getNumber());
            redirect.addFlashAttribute("message", "Order " + order.getNumber() + " placed. Thank you!");
            return "redirect:/orders/" + order.getNumber();
        } catch (CheckoutException e) {
            return showForm(model, form, e.getErrors());
        }
    }

    private String showForm(Model model, CheckoutForm form, List<String> errors) {
        model.addAttribute("cart", cart);
        model.addAttribute("form", form);
        model.addAttribute("deliveries", DeliveryMethod.values());
        model.addAttribute("payments", PaymentMethod.values());
        model.addAttribute("errors", errors);
        return "checkout";
    }

    @GetMapping("/orders")
    public String myOrders(Model model) {
        List<Order> orders = myOrders.getNumbers().stream()
                .map(checkout::findOrder).flatMap(Optional::stream).toList();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/orders/{number}")
    public String order(@PathVariable("number") String number, Model model, HttpServletResponse response) {
        Optional<Order> order = myOrder(number);
        if (order.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("order", order.get());
        model.addAttribute("emails", notifications.forOrder(order.get().getNumber()).stream()
                .filter(n -> n.channel().equals(Notification.EMAIL)).toList());
        return "order";
    }

    @PostMapping("/orders/{number}/pay")
    public String pay(@PathVariable("number") String number, RedirectAttributes redirect) {
        return act(number, redirect, () -> checkout.pay(number), "Payment successful");
    }

    @PostMapping("/orders/{number}/cancel")
    public String cancel(@PathVariable("number") String number, RedirectAttributes redirect) {
        return act(number, redirect, () -> checkout.cancel(number), "Order cancelled");
    }

    private String act(String number, RedirectAttributes redirect, Runnable action, String done) {
        if (myOrder(number).isEmpty()) {
            return "redirect:/orders";
        }
        try {
            action.run();
            redirect.addFlashAttribute("message", done);
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + number;
    }

    /** Customers see only the orders placed in their own session. */
    private Optional<Order> myOrder(String number) {
        return myOrders.contains(number) ? checkout.findOrder(number) : Optional.empty();
    }
}
