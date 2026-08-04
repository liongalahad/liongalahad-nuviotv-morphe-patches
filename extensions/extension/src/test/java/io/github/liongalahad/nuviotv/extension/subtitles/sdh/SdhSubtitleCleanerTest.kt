package io.github.liongalahad.nuviotv.extension.subtitles.sdh

import android.graphics.Color
import android.graphics.Bitmap
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.test.core.app.ApplicationProvider
import io.github.liongalahad.nuviotv.extension.settings.MorpheSettingsRuntime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SdhSubtitleCleanerTest {
    @Before fun resetPreference() = setEnabled(false)
    @After fun cleanUpPreference() = setEnabled(false)

    @Test fun `default and off mode preserve source identity`() {
        val source: CharSequence = "[door closes] JOHN: Hello."
        val group = CueGroup(listOf(Cue.Builder().setText(source).build()), 1L)
        assertSame(group, SdhCueTransformer.clean(group))
        assertSame(source, group.cues.single().text)
    }

    @Test fun `standalone sounds and no-dialogue annotations are suppressed`() {
        listOf("[door closes]", "[NO DIALOGUE]", "[no discernible speech]", "[panting]")
            .forEach { assertNull(it, SdhSubtitleCleaner.clean(it)) }
    }

    @Test fun `inline multiple full-width and parenthetical annotations are removed`() {
        assertEquals("Don't move.", cleaned("[whispers] Don't move."))
        assertEquals("Hello.", cleaned("[door opens] [footsteps]\nHello."))
        assertEquals("Come in.", cleaned("［door closes］ Come in."))
        assertEquals("Stay here.", cleaned("(whispering) Stay here."))
        assertEquals("Stay here.", cleaned("(in Italian) Stay here."))
    }

    @Test fun `speaker labels and qualifiers are removed`() {
        assertEquals("Where are you?", cleaned("JOHN: Where are you?"))
        assertEquals("Where are you?", cleaned("JOHN (ON PHONE): Where are you?"))
        assertEquals("- Yep.\n- Outside.", cleaned("- [Tom] Yep.\n- SARAH: Outside."))
    }

    @Test fun `music descriptions are removed while lyrics remain`() {
        assertNull(SdhSubtitleCleaner.clean("♪ tense instrumental music ♪"))
        assertNull(SdhSubtitleCleaner.clean("♫ MUSIC PLAYING ♫"))
        assertEquals("♪ Hello darkness, my old friend ♪", cleaned("♪ Hello darkness, my old friend ♪"))
    }

    @Test fun `times urls ratios punctuation dialogue markers and ordinary parentheses survive`() {
        listOf(
            "The rule is: never look back.",
            "At 10:30, leave the house.",
            "Visit https://example.com now.",
            "Use 16:9.",
            "I said (and I meant it) that I was leaving.",
            "- Ordinary dialogue."
        ).forEach { assertEquals(it, cleaned(it)) }
    }

    @Test fun `blank malformed and mutable inputs are safe`() {
        assertNull(SdhSubtitleCleaner.clean("  \t "))
        assertEquals("[door closes", cleaned("[door closes"))
        assertEquals("(whispering", cleaned("(whispering"))
        val source = StringBuilder("[door closes] Hello.")
        assertEquals("Hello.", cleaned(source))
        assertEquals("[door closes] Hello.", source.toString())
    }

    @Test fun `surviving Android spans are preserved`() {
        val source = SpannableString("[door closes] Hello.")
        val span = ForegroundColorSpan(Color.RED)
        source.setSpan(span, 14, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val result = SdhSubtitleCleaner.clean(source)
        assertTrue(result is Spanned)
        assertEquals("Hello.", result.toString())
        assertEquals(1, (result as Spanned).getSpans(0, result.length, ForegroundColorSpan::class.java).size)
    }

    @Test fun `cue group timing positioning and non-text cues survive`() {
        setEnabled(true)
        val dialogue = Cue.Builder().setText("[whispers] Hello.").setPosition(0.25f).build()
        val annotation = Cue.Builder().setText("[door closes]").build()
        val nonText = Cue.Builder().setBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)).build()
        val result = SdhCueTransformer.clean(CueGroup(listOf(dialogue, annotation, nonText), 987_654L))
        assertEquals(987_654L, result.presentationTimeUs)
        assertEquals(2, result.cues.size)
        assertEquals("Hello.", result.cues[0].text.toString())
        assertEquals(0.25f, result.cues[0].position)
        assertSame(nonText, result.cues[1])
    }

    @Test fun `preference changes apply to the next cue group and persist`() {
        val source = CueGroup(listOf(Cue.Builder().setText("[door closes]").build()), 3L)
        assertEquals(1, SdhCueTransformer.clean(source).cues.size)
        setEnabled(true)
        assertEquals(0, SdhCueTransformer.clean(source).cues.size)
        assertTrue(MorpheSettingsRuntime.isRemoveSdhEnabled())
    }

    private fun setEnabled(enabled: Boolean) {
        MorpheSettingsRuntime.setRemoveSdhEnabled(ApplicationProvider.getApplicationContext(), enabled)
    }

    private fun cleaned(text: CharSequence): String? = SdhSubtitleCleaner.clean(text)?.toString()
}
