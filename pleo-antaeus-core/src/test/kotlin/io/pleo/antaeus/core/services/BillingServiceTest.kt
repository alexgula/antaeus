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
    fun `returns zero if no invoices`() {
        every { invoiceService.fetchAll(any()) } returns listOf()
        val result = billingService.chargeAll()
        Assertions.assertEquals(0, result)
    }

    @Test
    fun `returns zero if payment provider is succeeding`() {
        every { paymentProvider.charge(any()) } returns true
        every { invoiceService.fetchAll(any()) } returns listOf(invoice())
        val result = billingService.chargeAll()
        Assertions.assertEquals(0, result)
    }

    @Test
    fun `returns non-zero if payment provider is failing`() {
        every { paymentProvider.charge(any()) } returns false
        every { invoiceService.fetchAll(any()) } returns listOf(invoice())
        val result = billingService.chargeAll()
        Assertions.assertEquals(1, result)
    }

    @Test
    fun `returns non-zero if payment provider is failing sometimes`() {
        every { paymentProvider.charge(any()) } returns true andThen false
        every { invoiceService.fetchAll(any()) } returns listOf(invoice(), invoice())
        val result = billingService.chargeAll()
        Assertions.assertEquals(1, result)
    }

    @Test
    fun `reads pending invoices`() {
        every { invoiceService.fetchAll(any()) } returns listOf()
        billingService.chargeAll()
        verify(exactly = 1) { invoiceService.fetchAll(eq(InvoiceStatus.PENDING)) }
    }

    @Test
    fun `performs no payment if no invoices`() {
        every { invoiceService.fetchAll(any()) } returns listOf()
        billingService.chargeAll()
        verify(exactly = 0) { paymentProvider.charge(any()) }
    }

    @Test
    fun `performs payment only for pending invoices`() {
        every { paymentProvider.charge(any()) } returns true
        every { invoiceService.fetchAll(any()) } returns listOf(invoice())
        billingService.chargeAll()
        verify(exactly = 1) { paymentProvider.charge(any()) }
    }

    private fun invoice() =
            Invoice(0, 0, Money(BigDecimal.valueOf(0), Currency.EUR), InvoiceStatus.PENDING)
}