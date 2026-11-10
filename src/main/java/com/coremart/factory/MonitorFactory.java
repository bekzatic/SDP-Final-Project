package com.coremart.factory;

import com.coremart.model.Category;
import com.coremart.model.Monitor;
import com.coremart.model.Product;

import java.util.List;

/** FACTORY METHOD pattern - a <b>Concrete Creator</b> that makes {@link Monitor}s. */
public class MonitorFactory extends ProductFactory {

    @Override
    protected Product createProduct(long id, ProductSpec s) {
        return new Monitor(id, s.getName(), s.getBrand(), s.getPrice(), s.getStock(), s.getDescription(),
                s.decimal("sizeInches"), s.text("resolution"), s.integer("refreshRateHz"),
                s.text("panelType"));
    }

    @Override
    public List<String> requiredAttributes() {
        return List.of("sizeInches", "resolution", "refreshRateHz", "panelType");
    }

    @Override
    public Category getCategory() {
        return Category.MONITOR;
    }
}
