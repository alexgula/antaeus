package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import java.math.BigDecimal

class BillingService(
    private val paymentProvider: PaymentProvider
) {
    fun chargeAll(): Boolean {
        return paymentProvider.charge(Invoice(0, 0, Money(BigDecimal.valueOf(0), Currency.EUR), InvoiceStatus.PAID))
    }
}