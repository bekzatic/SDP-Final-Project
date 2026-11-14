package com.coremart.web;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Category;
import com.coremart.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogPageTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();

    @Autowired
    private MockMvc mvc;

    private ResultActions page(String url) throws Exception {
        return mvc.perform(get(url)).andExpect(status().isOk()).andExpect(view().name("catalog"));
    }

    @Test
    void homeAndCatalogShowEveryProduct() throws Exception {
        for (String url : List.of("/", "/catalog")) {
            ResultActions result = page(url)
                    .andExpect(model().attribute("products", catalog.findAll()))
                    .andExpect(content().string(containsString("Products found: " + catalog.size())));
            for (Product p : catalog.findAll()) {
                result.andExpect(content().string(containsString(p.getName())));
            }
        }
    }

    @Test
    void categoryFilterShowsOnlyThatCategory() throws Exception {
        List<Product> monitors = catalog.findByCategory(Category.MONITOR);
        ResultActions result = page("/catalog?category=MONITOR")
                .andExpect(model().attribute("selected", Category.MONITOR))
                .andExpect(model().attribute("products", monitors))
                .andExpect(content().string(containsString("<h1>Monitors</h1>")));
        for (Product p : monitors) {
            result.andExpect(content().string(containsString(p.getName())));
        }
        for (Product p : catalog.findByCategory(Category.LAPTOP)) {
            result.andExpect(content().string(not(containsString(p.getName()))));
        }
    }

    @Test
    void categoryNameIsCaseInsensitive() throws Exception {
        page("/catalog?category=laptop")
                .andExpect(model().attribute("products", catalog.findByCategory(Category.LAPTOP)));
    }

    @Test
    void unknownCategoryShowsTheWholeCatalog() throws Exception {
        page("/catalog?category=TOASTER")
                .andExpect(model().attribute("products", catalog.findAll()));
        assertNull(CatalogController.parseCategory("TOASTER"));
        assertNull(CatalogController.parseCategory(" "));
        assertEquals(Category.DESKTOP_PC, CatalogController.parseCategory(" desktop_pc "));
    }

    @Test
    void cardsShowPriceStockAndLinkToTheProductPage() throws Exception {
        Product lowStock = catalog.findAll().stream()
                .filter(p -> p.getStock() > 0 && p.getStock() <= CatalogController.LOW_STOCK)
                .findFirst().orElseThrow();
        page("/catalog")
                .andExpect(content().string(containsString(lowStock.getFormattedPrice())))
                .andExpect(content().string(containsString("Low stock: " + lowStock.getStock() + " pcs.")))
                .andExpect(content().string(containsString("href=\"/product/" + lowStock.getId() + "\"")))
                .andExpect(content().string(containsString("/css/style.css")));
    }

    @Test
    void stylesheetIsServed() throws Exception {
        mvc.perform(get("/css/style.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".card")));
    }
}
