package com.coremart.web;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Category;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartPageTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();

    @Autowired
    private MockMvc mvc;

    private Product laptop() {
        return catalog.findByCategory(Category.LAPTOP).stream().filter(p -> p.getStock() >= 2).findFirst().orElseThrow();
    }

    private Product accessory() {
        return catalog.findByCategory(Category.ACCESSORY).stream().filter(p -> p.getStock() >= 2).findFirst().orElseThrow();
    }

    @Test
    void emptyCart() throws Exception {
        mvc.perform(get("/cart").session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Пока пусто.")))
                .andExpect(content().string(containsString("<span class=\"cart-count\">0</span>")));
    }

    @Test
    void productPageOffersServices() throws Exception {
        mvc.perform(get("/product/" + laptop().getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/cart/add\"")))
                .andExpect(content().string(containsString("value=\"WARRANTY_1Y\"")))
                .andExpect(content().string(containsString("value=\"SETUP\"")));
        mvc.perform(get("/product/" + accessory().getId()))
                .andExpect(content().string(not(containsString("value=\"SETUP\""))));
    }

    @Test
    void addProductWithServicesThenChangeAndRemove() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Product laptop = laptop();
        mvc.perform(post("/cart/add").session(session)
                        .param("productId", String.valueOf(laptop.getId()))
                        .param("quantity", "1")
                        .param("addOns", "WARRANTY_1Y", "GIFT_WRAP"))
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attributeExists("message"));

        mvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(laptop.getFullName())))
                .andExpect(content().string(containsString("+ Расширенная гарантия +1 год")))
                .andExpect(content().string(containsString("+ Подарочная упаковка")))
                .andExpect(content().string(containsString("<span class=\"cart-count\">1</span>")));

        mvc.perform(post("/cart/update").session(session).param("lineId", "1").param("quantity", "2"))
                .andExpect(redirectedUrl("/cart"));
        mvc.perform(get("/cart").session(session))
                .andExpect(content().string(containsString("<span class=\"cart-count\">2</span>")));

        mvc.perform(post("/cart/remove").session(session).param("lineId", "1"))
                .andExpect(redirectedUrl("/cart"));
        mvc.perform(get("/cart").session(session))
                .andExpect(content().string(containsString("Пока пусто.")));
    }

    @Test
    void promoCodes() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/cart/add").session(session).param("productId", String.valueOf(laptop().getId())));

        mvc.perform(post("/cart/promo").session(session).param("code", "nope"))
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attribute("error", "Промокод «nope» не найден"));

        mvc.perform(post("/cart/promo").session(session).param("code", "student10"))
                .andExpect(flash().attribute("message", "Промокод STUDENT10 применён"));
        mvc.perform(get("/cart").session(session))
                .andExpect(content().string(containsString("Промокод STUDENT10")))
                .andExpect(content().string(containsString("Скидка 10%")));

        mvc.perform(post("/cart/promo/remove").session(session)).andExpect(redirectedUrl("/cart"));
        mvc.perform(get("/cart").session(session))
                .andExpect(content().string(containsString("name=\"code\"")));
    }

    @Test
    void errorsSendTheCustomerBack() throws Exception {
        Product laptop = laptop();
        mvc.perform(post("/cart/add").param("productId", String.valueOf(laptop.getId()))
                        .param("quantity", String.valueOf(laptop.getStock() + 1)))
                .andExpect(redirectedUrl("/product/" + laptop.getId()))
                .andExpect(flash().attributeExists("error"));
        mvc.perform(post("/cart/add").param("productId", "999999"))
                .andExpect(redirectedUrl("/catalog"))
                .andExpect(flash().attribute("error", "Товар не найден"));
    }
}
