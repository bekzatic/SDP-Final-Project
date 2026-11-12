package com.coremart.order;

/** Who the order is for and where to bring it. Values are trimmed; checks are done at checkout. */
public record CustomerInfo(String name, String phone, String email, String address) {

    public CustomerInfo {
        name = clean(name);
        phone = clean(phone);
        email = clean(email);
        address = clean(address);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
