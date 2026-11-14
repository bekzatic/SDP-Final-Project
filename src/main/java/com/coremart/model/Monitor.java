package com.coremart.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** A computer monitor. */
public class Monitor extends Product {

    private final double sizeInches;
    private final String resolution;
    private final int refreshRateHz;
    private final String panelType;

    public Monitor(long id, String name, String brand, long price, int stock, String description,
                   double sizeInches, String resolution, int refreshRateHz, String panelType) {
        super(id, name, brand, price, stock, description);
        this.sizeInches = sizeInches;
        this.resolution = resolution;
        this.refreshRateHz = refreshRateHz;
        this.panelType = panelType;
    }

    public double getSizeInches() { return sizeInches; }
    public String getResolution() { return resolution; }
    public int getRefreshRateHz() { return refreshRateHz; }
    public String getPanelType() { return panelType; }

    @Override
    public Category getCategory() {
        return Category.MONITOR;
    }

    @Override
    public Map<String, String> getSpecs() {
        Map<String, String> specs = new LinkedHashMap<>();
        specs.put("Screen size", Specs.inches(sizeInches));
        specs.put("Resolution", resolution);
        specs.put("Refresh rate", refreshRateHz + " Hz");
        specs.put("Panel", panelType);
        return specs;
    }
}
