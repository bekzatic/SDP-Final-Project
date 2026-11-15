package com.coremart.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankPaymentAdapterTest {

    private final DemoBankApi bank = new DemoBankApi();
    private final PaymentGateway gateway = new BankPaymentAdapter(bank);

    @Test
    void chargeInTengeBecomesAnAuthorizationInTiyn() {
        String id = gateway.charge("CM-1001", 18_000);
        assertTrue(id.startsWith("TX-"));
        assertEquals(1_800_000, bank.amountMinorOf(id));
    }

    @Test
    void refundReversesTheTransactionOnce() {
        String id = gateway.charge("CM-1002", 5_000);
        assertFalse(bank.isReversed(id));
        gateway.refund(id);
        gateway.refund(id);
        assertTrue(bank.isReversed(id));
    }

    @Test
    void bankStatusCodesBecomeExceptions() {
        assertThrows(IllegalArgumentException.class, () -> gateway.charge("CM-1003", 0));
        assertThrows(IllegalArgumentException.class, () -> gateway.refund("TX-404"));
    }

    @Test
    void theBankItselfSpeaksADifferentLanguage() {
        assertEquals(DemoBankApi.WRONG_CURRENCY, bank.authorize("CM-1004", 100, "USD").statusCode());
        assertEquals(DemoBankApi.INVALID_AMOUNT, bank.authorize("CM-1004", 0, "KZT").statusCode());
        assertTrue(bank.authorize("CM-1004", 100, "KZT").approved());
    }
}
