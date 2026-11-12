package com.coremart.web;

import com.coremart.bundle.BundleCatalog;
import com.coremart.bundle.ProductBundle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
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
class BundlePageTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BundleCatalog bundles;

    @Test
    void readyMadeBundlesAreBuiltFromTheCatalog() {
        Map<String, ProductBundle> all = bundles.getAll();
        assertEquals(3, all.size());
        ProductBundle gaming = bundles.findById("gaming").orElseThrow();
        assertEquals(5, gaming.getProducts().size(), "nested keyboard and mouse bundle counts too");
        assertTrue(gaming.getPrice() < gaming.getFullPrice());
        assertTrue(bundles.findById("nope").isEmpty());
        assertTrue(bundles.findById(null).isEmpty());
    }

    @Test
    void bundlesPageShowsEveryBundleWithItsContents() throws Exception {
        ResultActions page = mvc.perform(get("/bundles"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Готовые комплекты")))
                .andExpect(content().string(containsString("— Logitech MX Keys S")));
        for (ProductBundle b : bundles.getAll().values()) {
            page.andExpect(content().string(containsString(b.getTitle())))
                    .andExpect(content().string(containsString(b.getFormattedPrice())))
                    .andExpect(content().string(containsString("Экономия " + b.getFormattedSavings())));
        }
        mvc.perform(get("/catalog")).andExpect(content().string(containsString("href=\"/bundles\"")));
    }

    @Test
    void bundleGoesIntoTheCartAsOneLine() throws Exception {
        MockHttpSession session = new MockHttpSession();
        ProductBundle study = bundles.findById("study").orElseThrow();
        mvc.perform(post("/cart/add-bundle").session(session).param("bundleId", "study"))
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attributeExists("message"));
        mvc.perform(get("/cart").session(session))
                .andExpect(content().string(containsString(study.getTitle())))
                .andExpect(content().string(containsString(study.getFormattedPrice())))
                .andExpect(content().string(containsString("<span class=\"cart-count\">1</span>")));
    }

    @Test
    void unknownBundle() throws Exception {
        mvc.perform(post("/cart/add-bundle").param("bundleId", "nope"))
                .andExpect(redirectedUrl("/bundles"))
                .andExpect(flash().attribute("error", "Комплект не найден"));
    }
}
