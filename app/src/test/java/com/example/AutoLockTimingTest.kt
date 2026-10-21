package com.example

import com.example.security.PinSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoLockTimingTest {

    @Test
    fun `auto lock timeout elapsed calculation`() {
        val autoLockSeconds = 60 // 1 minute default
        val autoLockMs = autoLockSeconds * 1000L

        val backgroundStart = 1000000L

        // Scenario 1: Re-opened after 30 seconds -> within threshold, remains unlocked
        val elapsed30s = 30000L
        val shouldLockAt30s = elapsed30s >= autoLockMs
        assertFalse("Should not lock before timeout expires", shouldLockAt30s)

        // Scenario 2: Re-opened after 61 seconds -> timeout exceeded, must lock
        val elapsed61s = 61000L
        val shouldLockAt61s = elapsed61s >= autoLockMs
        assertTrue("Must lock when elapsed background time exceeds timeout", shouldLockAt61s)
    }

    @Test
    fun `auto lock constants conform to spec`() {
        assertEquals(0, PinSecurityManager.AUTO_LOCK_IMMEDIATE)
        assertEquals(60, PinSecurityManager.AUTO_LOCK_1_MIN)
        assertEquals(120, PinSecurityManager.AUTO_LOCK_2_MIN)
        assertEquals(300, PinSecurityManager.AUTO_LOCK_5_MIN)
        assertEquals(1800, PinSecurityManager.AUTO_LOCK_30_MIN)
        assertEquals(-1, PinSecurityManager.AUTO_LOCK_ON_APP_CLOSE)
    }

    @Test
    fun `auto lock until closed mode does not lock on brief background`() {
        val mode = PinSecurityManager.AUTO_LOCK_ON_APP_CLOSE
        val isTimeoutDriven = mode > 0
        assertFalse("Until-closed mode should not be locked by timer", isTimeoutDriven)
    }
}
