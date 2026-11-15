package com.coremart.kit;

import com.coremart.model.Accessory;
import com.coremart.model.Monitor;
import com.coremart.model.Product;

import java.util.List;

/**
 * ABSTRACT FACTORY pattern - the <b>Abstract Factory</b>.
 * <p>
 * A setup kit is a family of products that must fit together: a computer, a monitor and the
 * peripherals. A gaming PC goes with a fast gaming monitor and a headset, a study laptop with
 * a simple office monitor and a mouse. Each concrete factory creates one whole family, so the
 * client can never mix parts of different kits by mistake. The client
 * ({@link com.coremart.bundle.BundleCatalog}) works only with this interface.
 */
public interface SetupKitFactory {

    String getCode();

    String getTitle();

    int getDiscountPercent();

    Product createComputer();

    Monitor createMonitor();

    List<Accessory> createPeripherals();
}
