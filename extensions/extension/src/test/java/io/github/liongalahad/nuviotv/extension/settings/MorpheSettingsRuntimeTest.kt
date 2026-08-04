package io.github.liongalahad.nuviotv.extension.settings

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorpheSettingsRuntimeTest {
    private lateinit var application: Application

    private enum class Category { EXPERIENCE, APPEARANCE }
    private enum class Destination { INLINE }
    private data class MinifiedSection(
        val a: Category,
        val b: Destination = Destination.INLINE
    )

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        MorpheSettingsRuntime.initialize(application)
        MorpheSettingsRuntime.settingsClosed()
    }

    @After
    fun tearDown() {
        MorpheSettingsRuntime.settingsClosed()
    }

    @Test
    fun `wrapped experience category opens Morphe settings`() {
        assertTrue(MorpheSettingsRuntime.openIfMorphe(MinifiedSection(Category.EXPERIENCE)))

        val intent = shadowOf(application).nextStartedActivity
        assertEquals(MorpheSettingsActivity::class.java.name, intent.component?.className)
    }

    @Test
    fun `other wrapped category is ignored`() {
        assertFalse(MorpheSettingsRuntime.openIfMorphe(MinifiedSection(Category.APPEARANCE)))
    }
}
