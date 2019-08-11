package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BillingServiceTest {
    private val paymentProvider = mockk<PaymentProvider> {
        every { charge(any()) } returns true
    }

    private val billingService = BillingService(paymentProvider = paymentProvider)

    @Test
    fun `returns true if payment provider is succeeding`() {
        val result = billingService.chargeAll()
        Assertions.assertEquals(true, result)
    }
}