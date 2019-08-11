package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider

class BillingService(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) {
    /**
     * Charges all available invoices
     *
     * @return true if there are no pending invoices left, false otherwise
     */
    fun chargeAll(): Boolean {
        return invoiceService.fetchAll()
                .map { paymentProvider.charge(it) }
                .all { it }
    }
}