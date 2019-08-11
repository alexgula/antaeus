package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
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
                .map { charge(it) }
                .count { !it }
    }

    private fun charge(invoice: Invoice): Boolean {
        val isSuccess = paymentProvider.charge(invoice)
        if (isSuccess) {
            // Assuming payment provider is idempotent, thus no additional logic if invoice update fails
            invoiceService.update(invoice.copy(status = InvoiceStatus.PAID))
        }
        return isSuccess
    }
}