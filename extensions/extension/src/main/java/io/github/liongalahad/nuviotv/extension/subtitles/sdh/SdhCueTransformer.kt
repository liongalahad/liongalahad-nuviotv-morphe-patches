package io.github.liongalahad.nuviotv.extension.subtitles.sdh

import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import io.github.liongalahad.nuviotv.extension.settings.MorpheSettingsRuntime

/** Transforms only text cues and preserves all Media3 cue/group metadata. */
object SdhCueTransformer {
    @JvmStatic
    fun clean(group: CueGroup): CueGroup {
        if (!MorpheSettingsRuntime.isRemoveSdhEnabled()) return group
        val cleaned = cleanCues(group.cues)
        return if (cleaned === group.cues) group else CueGroup(cleaned, group.presentationTimeUs)
    }

    @JvmStatic
    fun clean(cues: List<Cue>): List<Cue> {
        if (!MorpheSettingsRuntime.isRemoveSdhEnabled()) return cues
        return cleanCues(cues)
    }

    internal fun cleanCues(cues: List<Cue>): List<Cue> {
        var changed = false
        val output = ArrayList<Cue>(cues.size)
        cues.forEach { cue ->
            val source = cue.text
            if (source == null) {
                output += cue
            } else {
                val cleaned = SdhSubtitleCleaner.clean(source)
                when {
                    cleaned == null -> changed = true
                    cleaned === source -> output += cue
                    else -> {
                        changed = true
                        output += cue.buildUpon().setText(cleaned).build()
                    }
                }
            }
        }
        return if (changed) output else cues
    }
}
