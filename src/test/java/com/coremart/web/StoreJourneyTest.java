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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/** The whole store in one test: all nine patterns take part in one order. */
@SpringBootTest
@AutoConfigureMockMvc
class StoreJourneyTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();
    private final Map<Long, Integer> stockBefore = new HashMap<>();

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void rememberStock() {
        catalog.findAll().forEach(p -> stockBefore.put(p.getId(), p.getStock()));
    }

    @AfterEach
    void restoreStock() {
        catalog.findAll().forEach(p -> p.setStock(stockBefore.get(p.getId())));
    }

    @Test
    void fromCatalogToDeliveredOrder() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Product laptop = catalog.findByCategory(Category.LAPTOP).stream()
                .filter(p -> p.getStock() >= 2).findFirst().orElseThrow();

        // Singleton + Strategy: the catalog sorted by price
        mvc.perform(get("/catalog").param("category", "LAPTOP").param("sort", "price-asc").session(session))
                .andExpect(content().string(containsString(laptop.getName())));
        // Factory Method + Builder made the product; Decorator adds a warranty and setup
        mvc.perform(post("/cart/add").session(session).param("productId", String.valueOf(laptop.getId()))
                .param("addOns", "WARRANTY_1Y", "SETUP"));
        // Composite: a bundle with a nested bundle
        mvc.perform(post("/cart/add-bundle").session(session).param("bundleId", "home-office"));
        // Strategy: promo code
        mvc.perform(post("/cart/promo").session(session).param("code", "SALE15"));

        // Facade: checkout in one call
        String url = mvc.perform(post("/checkout").session(session)
                        .param("name", "Данияр").param("phone", "+7 705 111 22 33")
                        .param("email", "daniyar@example.com").param("address", "ул. Сатпаева, 22")
                        .param("delivery", "COURIER").param("payment", "LATER"))
                .andReturn().getResponse().getRedirectedUrl();
        String number = url.substring("/orders/".length());

        // State: pay, then the staff move the order on
        mvc.perform(post(url + "/pay").session(session));
        for (String action : new String[] {"start_assembly", "ship", "deliver"}) {
            mvc.perform(post("/admin/orders/" + number + "/" + action));
        }

        // Observer: the customer got an email about every step
        mvc.perform(get(url).session(session))
                .andExpect(content().string(containsString("status status-delivered")))
                .andExpect(content().string(containsString("+ Расширенная гарантия +1 год")))
                .andExpect(content().string(containsString("Домашний офис")))
                .andExpect(content().string(containsString("Промокод SALE15")))
                .andExpect(content().string(containsString("Заказ " + number + " оформлен.")))
                .andExpect(content().string(containsString("Заказ " + number + ": «Оплачен».")))
                .andExpect(content().string(containsString("Заказ " + number + ": «Собирается».")))
                .andExpect(content().string(containsString("Заказ " + number + ": «Передан курьеру».")))
                .andExpect(content().string(containsString("Заказ " + number + ": «Получен».")));
    }
}
