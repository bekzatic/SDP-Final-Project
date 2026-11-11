package com.coremart.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formatting helpers shared by the product classes. */
final class Specs {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("ru")));

    private Specs() {
    }

    /** 512 -> "512 ГБ SSD", 1000 -> "1 ТБ SSD". */
    static String storage(int gb) {
        return (gb >= 1000 && gb % 1000 == 0 ? (gb / 1000) + " ТБ" : gb + " ГБ") + " SSD";
    }

    /** 15.6 -> "15,6″". */
    static String inches(double value) {
        return decimal(value) + "″";
    }

    static String decimal(double value) {
        synchronized (DECIMAL) {
            return DECIMAL.format(value);
        }
    }
}
