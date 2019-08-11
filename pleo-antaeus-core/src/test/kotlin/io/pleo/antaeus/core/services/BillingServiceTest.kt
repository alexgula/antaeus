package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BillingServiceTest {
    private val paymentProvider = mockk<PaymentProvider>()

    private val billingService = BillingService(paymentProvider = paymentProvider)

    @Test
    fun `returns true if payment provider is succeeding`() {
        every { paymentProvider.charge(any()) } returns true
        val result = billingService.chargeAll()
        Assertions.assertEquals(true, result)
    }

    @Test
    fun `returns false if payment provider is failing`() {
        every { paymentProvider.charge(any()) } returns false
        val result = billingService.chargeAll()
        Assertions.assertEquals(false, result)
    }
}