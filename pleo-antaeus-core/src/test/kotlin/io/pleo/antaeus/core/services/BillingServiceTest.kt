package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BillingServiceTest {
    private val paymentProvider = mockk<PaymentProvider>()

    private val invoiceService = mockk<InvoiceService>()

    private val billingService = BillingService(paymentProvider = paymentProvider, invoiceService = invoiceService)

    @Test
    fun `returns true if no invoices`() {
        every { invoiceService.fetchAll() } returns listOf()
        val result = billingService.chargeAll()
        Assertions.assertEquals(true, result)
    }

    @Test
    fun `returns true if payment provider is succeeding`() {
        every { paymentProvider.charge(any()) } returns true
        every { invoiceService.fetchAll() } returns listOf(invoice())
        val result = billingService.chargeAll()
        Assertions.assertEquals(true, result)
    }

    @Test
    fun `returns false if payment provider is failing`() {
        every { paymentProvider.charge(any()) } returns false
        every { invoiceService.fetchAll() } returns listOf(invoice())
        val result = billingService.chargeAll()
        Assertions.assertEquals(false, result)
    }

    @Test
    fun `returns false if payment provider is failing sometimes`() {
        every { paymentProvider.charge(any()) } returns true andThen false
        every { invoiceService.fetchAll() } returns listOf(invoice(), invoice())
        val result = billingService.chargeAll()
        Assertions.assertEquals(false, result)
    }

    @Test
    fun `performs no payment if no invoices`() {
        every { invoiceService.fetchAll() } returns listOf()
        billingService.chargeAll()
        verify(exactly = 0) { paymentProvider.charge(any()) }
    }

    @Test
    fun `performs no payment if no pending invoices`() {
        every { invoiceService.fetchAll() } returns listOf(invoice(InvoiceStatus.PAID), invoice(InvoiceStatus.PAID))
        billingService.chargeAll()
        verify(exactly = 0) { paymentProvider.charge(any()) }
    }

    @Test
    fun `performs payment only for pending invoices`() {
        every { paymentProvider.charge(any()) } returns true
        every { invoiceService.fetchAll() } returns listOf(invoice(InvoiceStatus.PENDING), invoice(InvoiceStatus.PAID))
        billingService.chargeAll()
        verify(exactly = 1) { paymentProvider.charge(any()) }
    }

    private fun invoice(status: InvoiceStatus = InvoiceStatus.PENDING) =
            Invoice(0, 0, Money(BigDecimal.valueOf(0), Currency.EUR), status)
}