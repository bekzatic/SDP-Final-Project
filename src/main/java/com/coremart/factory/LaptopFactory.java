package com.coremart.factory;

import com.coremart.model.Category;
import com.coremart.model.Laptop;
import com.coremart.model.Product;

import java.util.List;

/** FACTORY METHOD pattern - a <b>Concrete Creator</b> that makes {@link Laptop}s. */
public class LaptopFactory extends ProductFactory {

    @Override
    protected Product createProduct(long id, ProductSpec s) {
        return new Laptop(id, s.getName(), s.getBrand(), s.getPrice(), s.getStock(), s.getDescription(),
                s.text("cpu"), s.integer("ramGb"), s.integer("storageGb"), s.text("gpu"),
                s.decimal("screenInches"), s.decimal("weightKg"));
    }

    @Override
    public List<String> requiredAttributes() {
        return List.of("cpu", "ramGb", "storageGb", "gpu", "screenInches", "weightKg");
    }

    @Override
    public Category getCategory() {
        return Category.LAPTOP;
    }
}
