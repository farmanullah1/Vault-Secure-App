package com.example

import com.example.security.PinSecurityManager
import com.example.security.SettingsRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRegistryTest {

    @Test
    fun `all settings have complete metadata`() {
        val all = SettingsRegistry.ALL_SETTINGS
        assertTrue("Settings registry should have registered settings", all.isNotEmpty())

        val keys = mutableSetOf<String>()
        for (setting in all) {
            assertTrue("Key cannot be blank: ${setting.key}", setting.key.isNotBlank())
            assertTrue("Key must be unique: ${setting.key}", keys.add(setting.key))
            assertTrue("Title cannot be blank for ${setting.key}", setting.title.isNotBlank())
            assertTrue("Description cannot be blank for ${setting.key}", setting.description.isNotBlank())
            assertTrue("Effect must be documented for ${setting.key}", setting.effect.isNotBlank())
            assertTrue("Test verification must be named for ${setting.key}", setting.testVerification.isNotBlank())
            assertNotNull("Default value cannot be null for ${setting.key}", setting.defaultValue)

            if (setting.allowedValues != null) {
                assertTrue(
                    "Default value must be in allowed values for ${setting.key}",
                    setting.allowedValues.contains(setting.defaultValue)
                )
            }
        }
    }

    @Test
    fun `default auto lock conforms to v4 spec 1 minute default`() {
        assertEquals(
            PinSecurityManager.AUTO_LOCK_1_MIN,
            SettingsRegistry.AUTO_LOCK_MODE.defaultValue
        )
        assertEquals(60, SettingsRegistry.AUTO_LOCK_MODE.defaultValue)
    }

    @Test
    fun `privacy defaults conform to secure baseline`() {
        assertTrue("FLAG_SECURE must be enabled by default", SettingsRegistry.FLAG_SECURE.defaultValue)
        assertTrue("Biometrics enabled by default if available", SettingsRegistry.BIOMETRICS_ENABLED.defaultValue)
        assertTrue("Keypad shuffle enabled by default", SettingsRegistry.SHUFFLE_KEYPAD.defaultValue)
        assertTrue("Lock on screen off enabled by default", SettingsRegistry.LOCK_ON_SCREEN_OFF.defaultValue)
        assertTrue("Stop on headphone disconnect enabled by default", SettingsRegistry.STOP_ON_HEADPHONE_DISCONNECT.defaultValue)
        assertTrue("Volume zero on disconnect enabled by default", SettingsRegistry.VOLUME_ZERO_ON_HEADPHONE_DISCONNECT.defaultValue)
        assertTrue("Pause on phone lock enabled by default", SettingsRegistry.PAUSE_ON_PHONE_LOCK.defaultValue)

        // Intruder photo must be OPT-IN by default per Spec 32 & 41
        assertFalse("Intruder photo must be opt-in (disabled by default)", SettingsRegistry.INTRUDER_PHOTO_ENABLED.defaultValue)
    }
}
