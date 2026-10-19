package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.PinSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SIM Services", appName)
    }

    @Test
    fun `pin security verification test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val securityManager = PinSecurityManager(context)

        // Set PIN
        assertTrue(securityManager.setPin("1234"))
        assertTrue(securityManager.isPinSetup)

        // Verify correct PIN
        val successResult = securityManager.verifyPin("1234")
        assertTrue(successResult is PinSecurityManager.VerificationResult.Success)

        // Verify wrong PIN
        val failedResult = securityManager.verifyPin("9999")
        assertTrue(failedResult is PinSecurityManager.VerificationResult.Failed)

        // Safe reset
        securityManager.safeResetAuth()
        assertFalse(securityManager.isPinSetup)
    }
}
