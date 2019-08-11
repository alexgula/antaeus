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
     * @return true if there are no pending invoices left, false otherwise
     */
    fun chargeAll(): Boolean {
        return invoiceService.fetchAll(InvoiceStatus.PENDING)
                .map { paymentProvider.charge(it) }
                .all { it }
    }
}