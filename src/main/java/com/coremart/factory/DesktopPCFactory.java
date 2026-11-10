package com.coremart.factory;

import com.coremart.model.Category;
import com.coremart.model.DesktopPC;
import com.coremart.model.Product;

import java.util.List;

/** FACTORY METHOD pattern - a <b>Concrete Creator</b> that makes {@link DesktopPC}s. */
public class DesktopPCFactory extends ProductFactory {

    @Override
    protected Product createProduct(long id, ProductSpec s) {
        return new DesktopPC(id, s.getName(), s.getBrand(), s.getPrice(), s.getStock(), s.getDescription(),
                s.text("cpu"), s.integer("ramGb"), s.integer("storageGb"), s.text("gpu"),
                s.text("formFactor"));
    }

    @Override
    public List<String> requiredAttributes() {
        return List.of("cpu", "ramGb", "storageGb", "gpu", "formFactor");
    }

    @Override
    public Category getCategory() {
        return Category.DESKTOP_PC;
    }
}
