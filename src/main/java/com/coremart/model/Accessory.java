package com.coremart.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** A peripheral: mouse, keyboard, headset, webcam and so on. */
public class Accessory extends Product {

    private final String type;
    private final String connection;

    public Accessory(long id, String name, String brand, long price, int stock, String description,
                     String type, String connection) {
        super(id, name, brand, price, stock, description);
        this.type = type;
        this.connection = connection;
    }

    public String getType() { return type; }
    public String getConnection() { return connection; }

    @Override
    public Category getCategory() {
        return Category.ACCESSORY;
    }

    @Override
    public Map<String, String> getSpecs() {
        Map<String, String> specs = new LinkedHashMap<>();
        specs.put("Тип", type);
        specs.put("Подключение", connection);
        return specs;
    }
}
