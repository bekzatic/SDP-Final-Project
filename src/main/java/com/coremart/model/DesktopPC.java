package com.coremart.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** A ready-made desktop computer (system unit). */
public class DesktopPC extends Product {

    private final String cpu;
    private final int ramGb;
    private final int storageGb;
    private final String gpu;
    private final String formFactor;

    public DesktopPC(long id, String name, String brand, long price, int stock, String description,
                     String cpu, int ramGb, int storageGb, String gpu, String formFactor) {
        super(id, name, brand, price, stock, description);
        this.cpu = cpu;
        this.ramGb = ramGb;
        this.storageGb = storageGb;
        this.gpu = gpu;
        this.formFactor = formFactor;
    }

    public String getCpu() { return cpu; }
    public int getRamGb() { return ramGb; }
    public int getStorageGb() { return storageGb; }
    public String getGpu() { return gpu; }
    public String getFormFactor() { return formFactor; }

    @Override
    public Category getCategory() {
        return Category.DESKTOP_PC;
    }

    @Override
    public Map<String, String> getSpecs() {
        Map<String, String> specs = new LinkedHashMap<>();
        specs.put("Processor", cpu);
        specs.put("RAM", ramGb + " GB");
        specs.put("Storage", Specs.storage(storageGb));
        specs.put("Graphics", gpu);
        specs.put("Form factor", formFactor);
        return specs;
    }
}
