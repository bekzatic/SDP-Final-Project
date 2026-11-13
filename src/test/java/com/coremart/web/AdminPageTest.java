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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPageTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();
    private final Map<Long, Integer> stockBefore = new HashMap<>();

    @Autowired
    private MockMvc mvc;

    private Product mouse;

    @BeforeEach
    void setUp() {
        catalog.findAll().forEach(p -> stockBefore.put(p.getId(), p.getStock()));
        mouse = catalog.findByCategory(Category.ACCESSORY).stream()
                .filter(p -> p.getStock() >= 5).findFirst().orElseThrow();
    }

    @AfterEach
    void restoreStock() {
        catalog.findAll().forEach(p -> p.setStock(stockBefore.get(p.getId())));
    }

    /** A customer places an order; returns its number. */
    private String placeOrder(Product product, int quantity, String payment) throws Exception {
        MockHttpSession customer = new MockHttpSession();
        mvc.perform(post("/cart/add").session(customer)
                .param("productId", String.valueOf(product.getId()))
                .param("quantity", String.valueOf(quantity)));
        String url = mvc.perform(post("/checkout").session(customer)
                        .param("name", "Ерлан").param("phone", "+7 702 765 43 21")
                        .param("email", "erlan@example.com").param("address", "пр. Достык, 5")
                        .param("delivery", "COURIER").param("payment", payment))
                .andReturn().getResponse().getRedirectedUrl();
        return url.substring("/orders/".length());
    }

    @Test
    void adminPageShowsOrdersStockAndAlerts() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Для сотрудников")))
                .andExpect(content().string(containsString(mouse.getFullName())))
                .andExpect(content().string(containsString("action=\"/admin/stock\"")));
    }

    @Test
    void staffMoveAPaidOrderToTheCustomer() throws Exception {
        String number = placeOrder(mouse, 1, "CARD_NOW");
        String base = "/admin/orders/" + number;
        mvc.perform(get("/admin"))
                .andExpect(content().string(containsString(number)))
                .andExpect(content().string(containsString("action=\"" + base + "/start_assembly\"")))
                .andExpect(content().string(containsString("action=\"" + base + "/cancel\"")));

        mvc.perform(post(base + "/ship")).andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attribute("error",
                        "Действие «Передать в доставку» недоступно: заказ в статусе «Оплачен»"));
        mvc.perform(post(base + "/start_assembly"))
                .andExpect(flash().attribute("message", "Заказ " + number + ": «Собирается»"));
        mvc.perform(post(base + "/ship"))
                .andExpect(flash().attribute("message", "Заказ " + number + ": «Передан курьеру»"));
        mvc.perform(post(base + "/cancel")).andExpect(flash().attributeExists("error"));
        mvc.perform(post(base + "/deliver"))
                .andExpect(flash().attribute("message", "Заказ " + number + ": «Получен»"));
        mvc.perform(get("/admin"))
                .andExpect(content().string(containsString("status status-delivered")));
    }

    @Test
    void staffCannotPayAndUnknownActionsAreRefused() throws Exception {
        String number = placeOrder(mouse, 1, "LATER");
        mvc.perform(post("/admin/orders/" + number + "/pay"))
                .andExpect(flash().attribute("error", "Это действие недоступно сотрудникам"));
        mvc.perform(post("/admin/orders/" + number + "/explode"))
                .andExpect(flash().attribute("error", "Это действие недоступно сотрудникам"));
        mvc.perform(post("/admin/orders/CM-999999/ship"))
                .andExpect(flash().attribute("error", "Заказ CM-999999 не найден"));
    }

    @Test
    void cancellingFromTheAdminPageReturnsStock() throws Exception {
        int before = mouse.getStock();
        String number = placeOrder(mouse, 2, "CARD_NOW");
        assertEquals(before - 2, mouse.getStock());
        mvc.perform(post("/admin/orders/" + number + "/cancel"))
                .andExpect(flash().attribute("message", "Заказ " + number + ": «Отменён»"));
        assertEquals(before, mouse.getStock());
        mvc.perform(get("/admin"))
                .andExpect(content().string(containsString("Заказ " + number + " отменён, товары возвращены на склад.")));
    }

    @Test
    void lowStockAlertAndRestock() throws Exception {
        Product rare = catalog.findAll().stream().filter(p -> p.getStock() >= 2 && p.getStock() <= 5)
                .findFirst().orElseThrow();
        String number = placeOrder(rare, rare.getStock() - 1, "LATER");
        mvc.perform(get("/admin"))
                .andExpect(content().string(containsString("Заканчивается «" + rare.getFullName() + "»: осталось 1 шт.")))
                .andExpect(content().string(containsString(number)));

        mvc.perform(post("/admin/stock").param("productId", String.valueOf(rare.getId())).param("quantity", "10"))
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attribute("message", "«" + rare.getFullName() + "»: на складе 11 шт."));
        assertEquals(11, rare.getStock());

        mvc.perform(post("/admin/stock").param("productId", String.valueOf(rare.getId())).param("quantity", "0"))
                .andExpect(flash().attribute("error", "Количество должно быть от 1 до 100"));
        mvc.perform(post("/admin/stock").param("productId", "999999").param("quantity", "1"))
                .andExpect(flash().attribute("error", "Товар не найден"));
    }
}
