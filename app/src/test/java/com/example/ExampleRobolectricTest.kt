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
    fun `read string from context conforms to spec`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SIM Service", appName)

        val simServiceTitle = context.getString(R.string.sim_service_title)
        assertEquals("SIM Service", simServiceTitle)

        val jazzLabel = context.getString(R.string.sim_1_jazz)
        assertEquals("Jazz", jazzLabel)

        val zongLabel = context.getString(R.string.sim_2_zong)
        assertEquals("Zong", zongLabel)
    }

    @Test
    fun `pin security verification test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val securityManager = PinSecurityManager(context)

        // Initial default auto-lock is 1 minute (60 seconds) per Spec Section 9.4
        assertEquals(PinSecurityManager.AUTO_LOCK_1_MIN, securityManager.autoLockSeconds)

        // Set PIN
        assertTrue(securityManager.setPin("1234"))
        assertTrue(securityManager.isPinSetup)

        // Verify correct PIN
        val successResult = securityManager.verifyPin("1234")
        assertTrue(successResult is PinSecurityManager.VerificationResult.Success)

        // Verify decoy PIN
        assertTrue(securityManager.setDecoyPin("5678"))
        assertTrue(securityManager.isDecoyPinSetup)
        val decoyResult = securityManager.verifyPin("5678")
        assertTrue(decoyResult is PinSecurityManager.VerificationResult.DecoySuccess)
        assertTrue(securityManager.isDecoySessionActive)

        // Verify wrong PIN
        val failedResult = securityManager.verifyPin("9999")
        assertTrue(failedResult is PinSecurityManager.VerificationResult.Failed)

        // Safe reset
        securityManager.safeResetAuth()
        assertFalse(securityManager.isPinSetup)
        assertFalse(securityManager.isDecoyPinSetup)
        assertEquals(PinSecurityManager.AUTO_LOCK_1_MIN, securityManager.autoLockSeconds)
    }
}
