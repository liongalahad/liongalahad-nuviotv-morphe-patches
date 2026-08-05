package io.github.liongalahad.nuviotv.extension.settings

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorpheSettingsRuntimeTest {
    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        MorpheSettingsRuntime.initialize(application)
        MorpheSettingsRuntime.setRemoveSdhEnabled(application, false)
    }

    @Test
    fun `visibility remap exposes only the hidden experience slot`() {
        assertTrue(MorpheSettingsRuntime.mapVisibilityOrdinal(0) == 4)
        assertTrue(MorpheSettingsRuntime.mapVisibilityOrdinal(3) == 3)
    }

    @Test
    fun `three modes commit synchronously and expose exact labels`() {
        assertEquals("Off", MorpheSettingsRuntime.sdhModeTitle(0))
        assertEquals("Remove SDH, keep lyrics", MorpheSettingsRuntime.sdhModeTitle(1))
        assertEquals("Full cleanup", MorpheSettingsRuntime.sdhModeTitle(2))

        MorpheSettingsRuntime.setSdhCleanupMode(application, 1)
        assertEquals(1, MorpheSettingsRuntime.sdhCleanupModeOrdinal())
        assertTrue(MorpheSettingsRuntime.isSdhModeSelected(1))
        assertFalse(MorpheSettingsRuntime.isSdhModeSelected(2))

        MorpheSettingsRuntime.setSdhCleanupMode(application, 2)
        assertEquals(2, MorpheSettingsRuntime.sdhCleanupModeOrdinal())
        assertTrue(MorpheSettingsRuntime.isRemoveSdhEnabled())

        MorpheSettingsRuntime.setSdhCleanupMode(application, 99)
        assertEquals(0, MorpheSettingsRuntime.sdhCleanupModeOrdinal())
        assertFalse(MorpheSettingsRuntime.isRemoveSdhEnabled())
    }
}
