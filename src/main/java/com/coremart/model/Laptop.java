package com.coremart.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** A notebook computer. */
public class Laptop extends Product {

    private final String cpu;
    private final int ramGb;
    private final int storageGb;
    private final String gpu;
    private final double screenInches;
    private final double weightKg;

    public Laptop(long id, String name, String brand, long price, int stock, String description,
                  String cpu, int ramGb, int storageGb, String gpu, double screenInches, double weightKg) {
        super(id, name, brand, price, stock, description);
        this.cpu = cpu;
        this.ramGb = ramGb;
        this.storageGb = storageGb;
        this.gpu = gpu;
        this.screenInches = screenInches;
        this.weightKg = weightKg;
    }

    public String getCpu() { return cpu; }
    public int getRamGb() { return ramGb; }
    public int getStorageGb() { return storageGb; }
    public String getGpu() { return gpu; }
    public double getScreenInches() { return screenInches; }
    public double getWeightKg() { return weightKg; }

    @Override
    public Category getCategory() {
        return Category.LAPTOP;
    }

    @Override
    public Map<String, String> getSpecs() {
        Map<String, String> specs = new LinkedHashMap<>();
        specs.put("Процессор", cpu);
        specs.put("Оперативная память", ramGb + " ГБ");
        specs.put("Накопитель", Specs.storage(storageGb));
        specs.put("Видеокарта", gpu);
        specs.put("Экран", Specs.inches(screenInches));
        specs.put("Вес", Specs.decimal(weightKg) + " кг");
        return specs;
    }
}
