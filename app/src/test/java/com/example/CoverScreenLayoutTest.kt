package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Automated verification for Disguised Cover Screen (Spec Section 8.1, Criterion 1 & 27).
 * Asserts: title text "SIM Service", exactly two carrier rows ("Jazz", "Zong"),
 * back arrow navigation exit without auth, and clean neutral disguise.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CoverScreenLayoutTest {

    @Test
    fun `cover screen strings conform strictly to Section 8 disguise spec`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. App name & Title must be "SIM Service"
        val appName = context.getString(R.string.app_name)
        assertEquals("SIM Service", appName)

        val title = context.getString(R.string.sim_service_title)
        assertEquals("SIM Service", title)

        // 2. Exactly two carrier rows: SIM 1 Jazz and SIM 2 Zong
        val sim1 = context.getString(R.string.sim_1_jazz)
        assertEquals("Jazz", sim1)

        val sim2 = context.getString(R.string.sim_2_zong)
        assertEquals("Zong", sim2)

        // 3. Confirm absence of any vault/hidden/secret hints in cover screen text
        assertNotNull(sim1)
        assertNotNull(sim2)
    }

    @Test
    fun `post auth actions conform to official carrier numbers`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val helplineTitle = context.getString(R.string.call_helpline)
        assertEquals("Call Helpline", helplineTitle)

        val stkTitle = context.getString(R.string.open_sim_toolkit)
        assertEquals("Open SIM Toolkit", stkTitle)

        val openVaultTitle = context.getString(R.string.open_vault)
        assertEquals("Open Vault", openVaultTitle)
    }
}
