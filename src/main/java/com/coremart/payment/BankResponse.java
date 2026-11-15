package com.coremart.payment;

/** Answer of the bank API: a transaction reference and a status code (0 means approved). */
public record BankResponse(String transactionRef, int statusCode, String message) {

    public static final int APPROVED = 0;

    public boolean approved() {
        return statusCode == APPROVED;
    }
}
