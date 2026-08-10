package io.github.liongalahad.nuviotv.extension.subtitles.localstoragesubtitles

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalSubtitleRuntimeTest {
    data class FakeSubtitle(
        val id: String,
        val url: String,
        val lang: String = LocalSubtitleRuntime.LOCAL_LANGUAGE_KEY,
        val addonName: String = LocalSubtitleRuntime.LOCAL_SOURCE_LABEL
    )

    class FakeRailItem(private val key: String) {
        fun a(): String = key
    }

    @Test fun `TTML metadata takes priority over filename`() {
        assertEquals(
            "it",
            LocalSubtitleRuntime.inferLanguageCode(
                "Movie.en.ttml",
                "<tt xmlns=\"http://www.w3.org/ns/ttml\" xml:lang=\"it\"><body/></tt>"
            )
        )
    }

    @Test fun `ASS declared language is detected`() {
        assertEquals(
            "en",
            LocalSubtitleRuntime.inferLanguageCode(
                "subtitle.ass",
                "[Script Info]\nLanguage: eng\n[V4+ Styles]"
            )
        )
    }

    @Test fun `language tokens are inferred conservatively from filenames`() {
        assertEquals("en", LocalSubtitleRuntime.inferLanguageCode("Movie.Name.en.srt", ""))
        assertEquals("it", LocalSubtitleRuntime.inferLanguageCode("Movie_Name_ita.vtt", ""))
        assertEquals("en", LocalSubtitleRuntime.inferLanguageCode("Movie.Name (eng-hi).srt", ""))
        assertEquals(
            "und",
            LocalSubtitleRuntime.inferLanguageCode(
                "Widows.Bay.S01E02.1080p.WEB.h264-GRACE (SDH).srt",
                "They're so beautiful, Tom."
            )
        )
        assertEquals(
            "und",
            LocalSubtitleRuntime.inferLanguageCode(
                "Widows.Bay.S01E02.1080p.WEB.h264-GRACE.srt",
                "They're so beautiful, Tom."
            )
        )
    }

    @Test fun `explicit metadata can still declare Southern Kurdish`() {
        assertEquals(
            "sdh",
            LocalSubtitleRuntime.inferLanguageCode(
                "subtitle.srt",
                "Language: sdh\n"
            )
        )
    }

    @Test fun `import refresh epoch advances`() {
        LocalSubtitleRefreshState.resetForTests()
        val before = LocalSubtitleRefreshState.observe()

        LocalSubtitleRefreshState.invalidate()

        assertEquals(before + 1, LocalSubtitleRefreshState.observe())
    }

    @Test fun `pending import refresh rebuilds the frozen addon session list once`() {
        val field = LocalSubtitleRuntime::class.java.getDeclaredField("sessionAddonListRefreshPending")
        field.isAccessible = true
        field.setBoolean(null, true)

        assertTrue(LocalSubtitleRuntime.refreshSessionAddonList(false))
        assertFalse(LocalSubtitleRuntime.refreshSessionAddonList(false))
        assertTrue(LocalSubtitleRuntime.refreshSessionAddonList(true))
    }

    @Test fun `local UI labels preserve SDH and expose only the filename`() {
        val subtitle = FakeSubtitle(
            id = "en\nMovie.Name.en.srt",
            url = "file:///data/user/0/com.nuvio.tv/cache/Movie.Name.en.srt"
        )

        assertEquals("Local Storage", LocalSubtitleRuntime.rewriteLanguageLabel("!LOCAL", "!local"))
        assertEquals("English", LocalSubtitleRuntime.rewriteOptionTitle("!LOCAL", subtitle))
        assertEquals("English SDH", LocalSubtitleRuntime.rewriteOptionTitle("!LOCAL SDH", subtitle))
        assertEquals("Movie.Name.en.srt", LocalSubtitleRuntime.rewriteOptionMeta(subtitle.id, subtitle))
        assertEquals("en", LocalSubtitleRuntime.playbackLanguage("!local", subtitle))
        assertEquals(2, LocalSubtitleRuntime.adjustLanguageCount("!local", 3))
    }

    @Test fun `picker action is not treated as an imported subtitle`() {
        val action = FakeSubtitle(id = "!local", url = "")
        val imported = FakeSubtitle(id = "und\nMovie.srt", url = "file:///private/Movie.srt")

        assertEquals("Choose subtitle file", LocalSubtitleRuntime.rewriteOptionTitle("!LOCAL", action))
        assertEquals("", LocalSubtitleRuntime.rewriteOptionMeta(action.id, action))
        assertFalse(LocalSubtitleRuntime.isImportedSubtitle(action))
        assertFalse(LocalSubtitleRuntime.selectableOptionState(true, action))
        assertTrue(LocalSubtitleRuntime.selectableOptionState(true, imported))
        assertFalse(LocalSubtitleRuntime.selectableOptionState(false, imported))
    }

    @Test fun `local storage is placed directly after none`() {
        val none = FakeRailItem("__off__")
        val english = FakeRailItem("en")
        val italian = FakeRailItem("it")
        val local = FakeRailItem("!local")

        assertEquals(
            listOf(none, local, english, italian),
            LocalSubtitleRuntime.prioritizeLanguageRail(listOf(none, english, italian, local))
        )
    }

    @Test fun `ordinary addon subtitles remain unchanged`() {
        val addon = FakeSubtitle(
            id = "123",
            url = "https://example.test/subtitle.srt",
            lang = "en",
            addonName = "OpenSubtitles"
        )

        assertEquals("English", LocalSubtitleRuntime.rewriteOptionTitle("English", addon))
        assertEquals("123", LocalSubtitleRuntime.rewriteOptionMeta("123", addon))
        assertEquals("en", LocalSubtitleRuntime.playbackLanguage("en", addon))
        assertFalse(LocalSubtitleRuntime.isImportedSubtitle(addon))
    }

    @Test fun `local selection suppresses stale restore until another choice`() {
        val movie = LocalSubtitleRuntime.contentKeyForTesting("movie-a", null, null)
        val storedFile = File("/data/user/0/com.nuvio.tv/files/Movie.srt")
        val stored = LocalSubtitleRuntime.ImportedSubtitle(
            "Movie.srt", "und", storedFile, 1_000L, movie
        )
        val imported = FakeSubtitle(
            id = "und\nMovie.srt",
            url = LocalSubtitleRuntime.storedFileUrlForTesting(storedFile)
        )
        val addon = FakeSubtitle(
            id = "123",
            url = "https://example.test/subtitle.srt",
            lang = "en",
            addonName = "OpenSubtitles"
        )

        try {
            LocalSubtitleRuntime.setImportStateForTesting(stored, movie)
            assertFalse(LocalSubtitleRuntime.rejectImportedSubtitleForMpv(imported))
            assertTrue(LocalSubtitleRuntime.importedSelectionActiveForTesting())
            assertTrue(LocalSubtitleRuntime.shouldSuppressTrackPreferenceRestore())
            assertFalse(LocalSubtitleRuntime.rejectImportedSubtitleForMpv(addon))
            assertFalse(LocalSubtitleRuntime.importedSelectionActiveForTesting())
            assertFalse(LocalSubtitleRuntime.shouldSuppressTrackPreferenceRestore())
        } finally {
            LocalSubtitleRuntime.setImportStateForTesting(null, null)
        }
    }

    @Test fun `stale local row from another movie cannot be selected`() {
        val movieA = LocalSubtitleRuntime.contentKeyForTesting("movie-a", null, null)
        val movieB = LocalSubtitleRuntime.contentKeyForTesting("movie-b", null, null)
        val storedFile = File("/data/user/0/com.nuvio.tv/files/Movie.srt")
        val stored = LocalSubtitleRuntime.ImportedSubtitle(
            "Movie.srt", "und", storedFile, 1_000L, movieA
        )
        val staleRow = FakeSubtitle(
            id = "und\nMovie.srt",
            url = LocalSubtitleRuntime.storedFileUrlForTesting(storedFile)
        )

        try {
            LocalSubtitleRuntime.setImportStateForTesting(stored, movieB)
            assertTrue(LocalSubtitleRuntime.rejectImportedSubtitleForMpv(staleRow))
            assertFalse(LocalSubtitleRuntime.importedSelectionActiveForTesting())
            assertFalse(LocalSubtitleRuntime.shouldSuppressTrackPreferenceRestore())
        } finally {
            LocalSubtitleRuntime.setImportStateForTesting(null, null)
        }
    }

    @Test fun `reopened local section starts from the language rail`() {
        assertTrue(LocalSubtitleRuntime.preferLanguageRailFocus("!local"))
        assertFalse(LocalSubtitleRuntime.preferLanguageRailFocus("en"))
        assertFalse(LocalSubtitleRuntime.preferLanguageRailFocus("__off__"))
    }

    @Test fun `private imports expire after seven unused days`() {
        val day = 24L * 60L * 60L * 1000L
        val importedAt = 1_000_000L

        assertFalse(LocalSubtitleRuntime.isExpiredForTesting(importedAt, importedAt + 7L * day - 1L))
        assertTrue(LocalSubtitleRuntime.isExpiredForTesting(importedAt, importedAt + 7L * day))
        assertFalse(LocalSubtitleRuntime.isExpiredForTesting(importedAt, importedAt - day))
    }

    @Test fun `movie import is visible only for its owning movie`() {
        val movieA = LocalSubtitleRuntime.contentKeyForTesting("movie-a", null, null)
        val movieB = LocalSubtitleRuntime.contentKeyForTesting("movie-b", null, null)
        val imported = LocalSubtitleRuntime.ImportedSubtitle(
            "Movie.en.srt",
            "en",
            File("Movie.en.srt"),
            1_000L,
            movieA
        )

        assertTrue(LocalSubtitleRuntime.belongsToContent(imported, movieA))
        assertFalse(LocalSubtitleRuntime.belongsToContent(imported, movieB))
    }

    @Test fun `missing playback identity cannot become a shared import owner`() {
        assertEquals("", LocalSubtitleRuntime.contentKeyForTesting(null, null, null))
        assertEquals("", LocalSubtitleRuntime.contentKeyForTesting("  ", 1, 2))
    }

    @Test fun `series import is isolated to the exact season and episode`() {
        val episode = LocalSubtitleRuntime.contentKeyForTesting("series-a", 1, 2)
        val nextEpisode = LocalSubtitleRuntime.contentKeyForTesting("series-a", 1, 3)
        val otherSeason = LocalSubtitleRuntime.contentKeyForTesting("series-a", 2, 2)
        val imported = LocalSubtitleRuntime.ImportedSubtitle(
            "Series.S01E02.en.srt",
            "en",
            File("Series.S01E02.en.srt"),
            1_000L,
            episode
        )

        assertTrue(LocalSubtitleRuntime.belongsToContent(imported, episode))
        assertFalse(LocalSubtitleRuntime.belongsToContent(imported, nextEpisode))
        assertFalse(LocalSubtitleRuntime.belongsToContent(imported, otherSeason))
    }

    @Test fun `same filename can be imported independently for different videos`() {
        val movieA = LocalSubtitleRuntime.contentKeyForTesting("movie-a", null, null)
        val movieB = LocalSubtitleRuntime.contentKeyForTesting("movie-b", null, null)
        val imported = LocalSubtitleRuntime.ImportedSubtitle(
            "English.srt",
            "en",
            File("stored-English.srt"),
            1_000L,
            movieA
        )

        assertTrue(LocalSubtitleRuntime.sameImportSlot(imported, "english.SRT", movieA))
        assertFalse(LocalSubtitleRuntime.sameImportSlot(imported, "English.srt", movieB))
    }

    @Test fun `legacy import stays hidden until claimed by its saved content`() {
        val movieA = LocalSubtitleRuntime.contentKeyForTesting("movie-a", null, null)
        val movieB = LocalSubtitleRuntime.contentKeyForTesting("movie-b", null, null)
        val imported = LocalSubtitleRuntime.ImportedSubtitle(
            "Legacy.srt",
            "und",
            File("Legacy.srt"),
            1_000L,
            ""
        )

        assertFalse(LocalSubtitleRuntime.belongsToContent(imported, movieA))
        assertTrue(LocalSubtitleRuntime.claimOwnerIfUnassigned(imported, movieA))
        assertTrue(LocalSubtitleRuntime.belongsToContent(imported, movieA))
        assertFalse(LocalSubtitleRuntime.belongsToContent(imported, movieB))
        assertFalse(LocalSubtitleRuntime.claimOwnerIfUnassigned(imported, movieB))
    }

    @Test fun `supported language names remain human readable`() {
        assertTrue(LocalSubtitleRuntime.displayLanguage("en").contains("English", ignoreCase = true))
        assertTrue(LocalSubtitleRuntime.displayLanguage("it").contains("Italian", ignoreCase = true))
        assertEquals("Unknown language", LocalSubtitleRuntime.displayLanguage("und"))
    }
}
