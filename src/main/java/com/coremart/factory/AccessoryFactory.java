package com.coremart.factory;

import com.coremart.model.Accessory;
import com.coremart.model.Category;
import com.coremart.model.Product;

import java.util.List;

/** FACTORY METHOD pattern - a <b>Concrete Creator</b> that makes {@link Accessory} items. */
public class AccessoryFactory extends ProductFactory {

    @Override
    protected Product createProduct(long id, ProductSpec s) {
        return new Accessory(id, s.getName(), s.getBrand(), s.getPrice(), s.getStock(), s.getDescription(),
                s.text("type"), s.text("connection"));
    }

    @Override
    public List<String> requiredAttributes() {
        return List.of("type", "connection");
    }

    @Override
    public Category getCategory() {
        return Category.ACCESSORY;
    }
}
