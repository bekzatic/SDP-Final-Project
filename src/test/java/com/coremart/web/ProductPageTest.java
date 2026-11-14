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

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class ProductPageTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();

    @Autowired
    private MockMvc mvc;

    @Test
    void everyProductPageShowsAllItsSpecs() throws Exception {
        for (Product p : catalog.findAll()) {
            ResultActions result = mvc.perform(get("/product/" + p.getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("product"))
                    .andExpect(model().attribute("product", p))
                    .andExpect(content().string(containsString(p.getName())))
                    .andExpect(content().string(containsString(p.getBrand())))
                    .andExpect(content().string(containsString(p.getFormattedPrice())));
            for (Map.Entry<String, String> spec : p.getSpecs().entrySet()) {
                result.andExpect(content().string(containsString(spec.getKey())));
            }
        }
    }

    @Test
    void productPageHasStockStatusAndLinksBack() throws Exception {
        Product monitor = catalog.findByCategory(Category.MONITOR).get(0);
        mvc.perform(get("/product/" + monitor.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Back to catalog")))
                .andExpect(content().string(containsString("href=\"/catalog\"")))
                .andExpect(content().string(containsString("href=\"/catalog?category=MONITOR\"")))
                .andExpect(content().string(containsString("Specifications")))
                .andExpect(content().string(containsString(monitor.getStock() + " pcs.")));
    }

    @Test
    void unknownProductGives404Page() throws Exception {
        for (String id : new String[] {"999999", "-1", "abc"}) {
            mvc.perform(get("/product/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(view().name("error/404"))
                    .andExpect(content().string(containsString("Page not found")))
                    .andExpect(content().string(containsString("Go to catalog")));
        }
    }

    @Test
    void unknownUrlGives404() throws Exception {
        mvc.perform(get("/no-such-page")).andExpect(status().isNotFound());
    }
}
