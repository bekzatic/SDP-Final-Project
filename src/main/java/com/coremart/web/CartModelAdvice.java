package com.coremart.web;

import com.coremart.cart.Cart;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Puts the number of items in the cart into every page, for the header link. */
@ControllerAdvice
public class CartModelAdvice {

    private final Cart cart;

    public CartModelAdvice(Cart cart) {
        this.cart = cart;
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        return cart.getItemCount();
    }
}
