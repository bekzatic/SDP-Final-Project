package com.coremart.checkout;

import com.coremart.order.CustomerInfo;
import com.coremart.order.DeliveryMethod;
import com.coremart.order.PaymentMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** What the customer types on the checkout page. */
public class CheckoutForm {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private String name = "";
    private String phone = "";
    private String email = "";
    private String address = "";
    private DeliveryMethod delivery = DeliveryMethod.PICKUP;
    private PaymentMethod payment = PaymentMethod.CARD_NOW;

    public CheckoutForm() {
    }

    public CheckoutForm(String name, String phone, String email, String address,
                        DeliveryMethod delivery, PaymentMethod payment) {
        setName(name);
        setPhone(phone);
        setEmail(email);
        setAddress(address);
        setDelivery(delivery);
        setPayment(payment);
    }

    /** Problems to show to the customer; empty when the form is fine. */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (name.length() < 2) {
            errors.add("Enter your name");
        }
        long digits = phone.chars().filter(Character::isDigit).count();
        if (digits < 10 || digits > 12 || !phone.matches("[0-9+()\\-\\s]+")) {
            errors.add("Enter a phone number, e.g. +7 701 123 45 67");
        }
        if (!EMAIL.matcher(email).matches()) {
            errors.add("Enter an email for order notifications");
        }
        if (delivery.needsAddress() && address.length() < 5) {
            errors.add("Enter a delivery address");
        }
        return errors;
    }

    public CustomerInfo toCustomer() {
        return new CustomerInfo(name, phone, email, delivery.needsAddress() ? address : "");
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public DeliveryMethod getDelivery() { return delivery; }
    public PaymentMethod getPayment() { return payment; }

    public void setName(String name) { this.name = clean(name); }
    public void setPhone(String phone) { this.phone = clean(phone); }
    public void setEmail(String email) { this.email = clean(email); }
    public void setAddress(String address) { this.address = clean(address); }

    public void setDelivery(DeliveryMethod delivery) {
        this.delivery = delivery == null ? DeliveryMethod.PICKUP : delivery;
    }

    public void setPayment(PaymentMethod payment) {
        this.payment = payment == null ? PaymentMethod.CARD_NOW : payment;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
