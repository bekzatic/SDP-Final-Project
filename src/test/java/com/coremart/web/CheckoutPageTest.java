package com.coremart.web;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Category;
import com.coremart.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CheckoutPageTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();
    private final Map<Long, Integer> stockBefore = new HashMap<>();

    @Autowired
    private MockMvc mvc;

    private MockHttpSession session;
    private Product mouse;

    @BeforeEach
    void setUp() {
        catalog.findAll().forEach(p -> stockBefore.put(p.getId(), p.getStock()));
        session = new MockHttpSession();
        mouse = catalog.findByCategory(Category.ACCESSORY).stream()
                .filter(p -> p.getStock() >= 5).findFirst().orElseThrow();
    }

    /** Orders take products from the shared catalog; put the stock back for the other tests. */
    @AfterEach
    void restoreStock() {
        catalog.findAll().forEach(p -> p.setStock(stockBefore.get(p.getId())));
    }

    private void addMouse(int quantity) throws Exception {
        mvc.perform(post("/cart/add").session(session)
                .param("productId", String.valueOf(mouse.getId()))
                .param("quantity", String.valueOf(quantity)));
    }

    /** Places an order and returns its page address, e.g. /orders/CM-1001. */
    private String checkout(String delivery, String payment) throws Exception {
        String url = mvc.perform(post("/checkout").session(session)
                        .param("name", "Aigerim")
                        .param("phone", "+7 701 123 45 67")
                        .param("email", "aigerim@example.com")
                        .param("address", "10 Abai St.")
                        .param("delivery", delivery)
                        .param("payment", payment))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andReturn().getResponse().getRedirectedUrl();
        assertTrue(url.startsWith("/orders/CM-"), url);
        return url;
    }

    @Test
    void emptyCartCannotBeCheckedOut() throws Exception {
        mvc.perform(get("/checkout").session(session))
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void cartLeadsToTheCheckoutForm() throws Exception {
        addMouse(1);
        mvc.perform(get("/cart").session(session))
                .andExpect(content().string(containsString("href=\"/checkout\"")));
        mvc.perform(get("/checkout").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Checkout")))
                .andExpect(content().string(containsString(mouse.getFullName() + " × 1")))
                .andExpect(content().string(containsString("value=\"COURIER\"")))
                .andExpect(content().string(containsString("value=\"LATER\"")));
    }

    @Test
    void wrongDataShowsTheFormAgain() throws Exception {
        addMouse(1);
        mvc.perform(post("/checkout").session(session)
                        .param("name", "A").param("phone", "12").param("email", "x")
                        .param("delivery", "COURIER").param("payment", "CARD_NOW"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please check your details")))
                .andExpect(content().string(containsString("Enter a delivery address")))
                .andExpect(content().string(containsString("value=\"12\"")));
        assertEquals(stockBefore.get(mouse.getId()), mouse.getStock());
    }

    @Test
    void unknownOptionsFallBackToDefaults() throws Exception {
        addMouse(1);
        String url = checkout("TELEPORT", "BARTER");
        mvc.perform(get(url).session(session))
                .andExpect(content().string(containsString("Store pickup")))
                .andExpect(content().string(containsString("Paid")));
    }

    @Test
    void payNowOrderIsPaidAndListed() throws Exception {
        addMouse(2);
        String url = checkout("COURIER", "CARD_NOW");
        String number = url.substring("/orders/".length());

        assertEquals(stockBefore.get(mouse.getId()) - 2, mouse.getStock());
        mvc.perform(get(url).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Order " + number + " placed. Thank you!")))
                .andExpect(content().string(containsString("status status-paid")))
                .andExpect(content().string(containsString("Paid, payment PAY-")))
                .andExpect(content().string(containsString("City courier")))
                .andExpect(content().string(containsString("Order " + number + ": “Paid”.")))
                .andExpect(content().string(containsString("<span class=\"cart-count\">0</span>")))
                .andExpect(content().string(not(containsString("action=\"" + url + "/pay\""))));
        mvc.perform(get("/orders").session(session))
                .andExpect(content().string(containsString("Order " + number)))
                .andExpect(content().string(containsString("Orders: 1")));
    }

    @Test
    void payLaterThenPayOrCancel() throws Exception {
        addMouse(1);
        String url = checkout("PICKUP", "LATER");
        mvc.perform(get(url).session(session))
                .andExpect(content().string(containsString("Awaiting payment")))
                .andExpect(content().string(containsString("action=\"" + url + "/pay\"")))
                .andExpect(content().string(containsString("action=\"" + url + "/cancel\"")));

        mvc.perform(post(url + "/pay").session(session))
                .andExpect(redirectedUrl(url))
                .andExpect(flash().attribute("message", "Payment successful"));
        mvc.perform(post(url + "/pay").session(session))
                .andExpect(flash().attributeExists("error"));

        mvc.perform(post(url + "/cancel").session(session))
                .andExpect(flash().attribute("message", "Order cancelled"));
        mvc.perform(get(url).session(session))
                .andExpect(content().string(containsString("status status-cancelled")))
                .andExpect(content().string(containsString("The money will be returned to your card")));
        assertEquals(stockBefore.get(mouse.getId()), mouse.getStock(), "stock is back after cancelling");
    }

    @Test
    void otherVisitorsCannotSeeTheOrder() throws Exception {
        addMouse(1);
        String url = checkout("PICKUP", "LATER");
        MockHttpSession stranger = new MockHttpSession();
        mvc.perform(get(url).session(stranger)).andExpect(status().isNotFound());
        mvc.perform(post(url + "/cancel").session(stranger)).andExpect(redirectedUrl("/orders"));
        mvc.perform(get("/orders").session(stranger))
                .andExpect(content().string(containsString("No orders yet.")));
        mvc.perform(get("/orders/CM-999999").session(session)).andExpect(status().isNotFound());
    }
}
