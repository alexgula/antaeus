package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) {
    /**
     * Charges all available invoices
     *
     * @return number of pending invoices left
     */
    fun chargeAll(): Int {
        return invoiceService.fetchAll(InvoiceStatus.PENDING)
                .map { paymentProvider.charge(it) }
                .count { !it }
    }
}