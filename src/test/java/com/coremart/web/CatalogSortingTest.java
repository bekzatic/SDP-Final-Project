package com.coremart.web;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Category;
import com.coremart.sorting.PriceAscendingSort;
import com.coremart.sorting.PriceDescendingSort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogSortingTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();

    @Autowired
    private MockMvc mvc;

    @Test
    void catalogIsSortedByTheChosenStrategy() throws Exception {
        mvc.perform(get("/catalog?sort=price-asc"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("products", new PriceAscendingSort().sort(catalog.findAll())))
                .andExpect(content().string(containsString("<option value=\"price-asc\" selected=\"selected\">")));
    }

    @Test
    void sortingWorksTogetherWithTheCategoryFilter() throws Exception {
        mvc.perform(get("/catalog?category=LAPTOP&sort=price-desc"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("products",
                        new PriceDescendingSort().sort(catalog.findByCategory(Category.LAPTOP))))
                .andExpect(content().string(containsString("href=\"/catalog?category=MONITOR&amp;sort=price-desc\"")))
                .andExpect(content().string(containsString("name=\"category\" value=\"LAPTOP\"")));
    }

    @Test
    void unknownSortKeepsTheCatalogOrder() throws Exception {
        mvc.perform(get("/catalog?sort=random"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("products", catalog.findAll()))
                .andExpect(content().string(containsString("href=\"/catalog?category=MONITOR\"")));
    }
}
