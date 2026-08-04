package io.github.liongalahad.nuviotv.extension.settings

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
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
    fun `toggle commits and returns the new value`() {
        assertTrue(MorpheSettingsRuntime.toggleRemoveSdhEnabled())
        assertTrue(MorpheSettingsRuntime.isRemoveSdhEnabled())
        assertFalse(MorpheSettingsRuntime.toggleRemoveSdhEnabled())
        assertFalse(MorpheSettingsRuntime.isRemoveSdhEnabled())
    }
}
