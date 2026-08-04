package io.github.liongalahad.nuviotv.extension.subtitles.sdh

import android.text.SpannableStringBuilder
import android.text.Spanned

/** Conservative SDH cleaner. The input is never mutated and surviving spans are retained. */
object SdhSubtitleCleaner {
    private val bracketPattern = Regex("""(?:\[([^\]\r\n]{1,80})]|［([^］\r\n]{1,80})］)""")
    private val parentheticalPattern = Regex("""\(([^()\r\n]{1,60})\)""")
    private val pairedNotesPattern = Regex("""[♪♫]+\s*([^♪♫\r\n]{0,80}?)\s*[♪♫]+""")
    private val whitespacePattern = Regex("""\s+""")
    private val dialogueWordPattern = Regex(
        """\b(?:i|i'm|i’m|me|my|mine|we|our|ours|you|your|yours|he|him|his|she|her|hers|they|them|their|this|that|who|what|where|when|why|how|please)\b""",
        RegexOption.IGNORE_CASE
    )
    private val soundWordPattern = Regex(
        """\b(?:applause|cheers?|cheering|claps?|clapping|clicks?|clicking|laughs?|laughter|laughing|chuckles?|chuckling|sighs?|sighing|scoffs?|scoffing|sniffs?|sniffing|sniffles?|inhales?|inhaling|exhales?|exhaling|whispers?|whispering|murmurs?|murmuring|mumbles?|mumbling|cries|crying|sobs?|sobbing|coughs?|coughing|groans?|groaning|grunts?|grunting|moans?|moaning|gasps?|gasping|breathes?|breathing|pants?|panting|whimpers?|whimpering|screams?|screaming|shouts?|shouting|yells?|yelling|footsteps?|rings?|ringing|buzzes?|buzzing|beeps?|beeping|knocks?|knocking|opens?|opening|closes?|closing|slams?|slamming|shatters?|shattering|crashes?|crashing|rustles?|rustling|creaks?|creaking|squeaks?|squeaking|thuds?|thudding|bangs?|banging|clatters?|clattering|rattles?|rattling|jingles?|jingling|chimes?|chiming|honks?|honking|revs?|revving|screeches?|screeching|whooshes?|whooshing|splashes?|splashing|barks?|barking|meows?|meowing|thunder|gunshots?|explosions?|doorbell|inaudible|indistinct|indistinctly|silence|static)\b""",
        RegexOption.IGNORE_CASE
    )
    private val soundSubjectPattern = Regex(
        """\b(?:door|doors|phone|telephone|glass|window|windows|music|radio|television|tv|alarm|alarms|sirens?|engine|engines|crowd|audience|wind|rain|thunder|gun|guns|shot|shots|explosion|explosions|footsteps?|voices?|voice|noise|noises|dog|dogs|cat|cats)\b""",
        RegexOption.IGNORE_CASE
    )
    private val directParentheticalPattern = Regex(
        """^(?:sighs?|sighing|laughs?|laughter|laughing|chuckles?|chuckling|whispers?|whispering|cries|crying|sobs?|sobbing|coughs?|coughing|groans?|groaning|moans?|moaning|gasps?|gasping|breathes?|breathing|pants?|panting|screams?|screaming|shouts?|shouting|inaudible|indistinct|indistinctly)$""",
        RegexOption.IGNORE_CASE
    )
    private val languageDeliveryPattern = Regex("""^(?:in|speaking)\s+[\p{L}][\p{L}\- ]{1,24}$""", RegexOption.IGNORE_CASE)
    private val deviceDeliveryPattern = Regex("""^(?:over|on|through)\s+(?:the\s+)?(?:radio|television|tv|phone|telephone|speaker|intercom)$""", RegexOption.IGNORE_CASE)
    private val deliveryPattern = Regex("""^(?:angrily|softly|quietly|loudly|sarcastically|nervously|sadly|excitedly|calmly|tearfully|hesitantly)$""", RegexOption.IGNORE_CASE)
    private val speakerPrefixPattern = Regex("""^(\s*(?:[-–—]\s*)?)([\p{L}\p{N}][\p{L}\p{N}'’\- ]{0,31}(?:\s*\([^()\r\n]{1,24}\))?)\s*:\s*""")
    private val parentheticalQualifierPattern = Regex("""\s*\([^()]{1,24}\)\s*$""")
    private val musicWordPattern = Regex("""\b(?:music|instrumental|melody|theme|score|soundtrack|song|singing|sings|choir|orchestra|drums?|guitar|piano|violin|jazz|rock|classical)\b""", RegexOption.IGNORE_CASE)
    private val actionPattern = Regex("""\b(?:plays?|playing|continues?|continuing|starts?|starting|stops?|stopping|fades?|fading|swells?|swelling)\b""", RegexOption.IGNORE_CASE)
    private val musicMoodPattern = Regex("""\b(?:ominous|tense|dramatic|soft|quiet|loud|upbeat|somber|sad|romantic|suspenseful|eerie|gentle|background|sinister)\b""", RegexOption.IGNORE_CASE)
    private val negativePattern = Regex("""\b(?:editor|translator|translation|subtitle|caption|note|literal|sign reads?)\b""", RegexOption.IGNORE_CASE)
    private val noDialoguePattern = Regex("""^no\s+(?:(?:audible|discernible|intelligible)\s+)?(?:dialogue|dialog|speech|speaking|audio|sound)$""", RegexOption.IGNORE_CASE)
    private val speakerRolePattern = Regex("""^(?:man|woman|boy|girl|child|narrator|announcer|tv announcer|radio announcer|reporter|interviewer|host|operator|dispatcher|caller|spouse|husband|wife|mother|father|lawyer|attorney|judge|waiter|waitress|doctor|nurse|officer|detective|teacher|student|crowd|all|voice|voices|tom|john|mike|sarah)$""", RegexOption.IGNORE_CASE)
    private val speakerBlocklist = setOf("at", "chapter", "http", "https", "note", "ratio", "rule", "scene", "time", "visit", "warning")
    private val dialogueMarkers = setOf("-", "–", "—")

    @JvmStatic
    fun clean(text: CharSequence): CharSequence? {
        if (text.isEmpty()) return null
        val lines = ArrayList<CharSequence>()
        var changed = false
        splitLines(text).forEach { line ->
            val cleaned = cleanLine(line)
            if (cleaned == null) changed = true else {
                if (cleaned.toString() != line.toString()) changed = true
                lines += cleaned
            }
        }
        if (lines.isEmpty()) return null
        if (!changed) return text
        return joinLines(lines, text is Spanned)
    }

    private fun cleanLine(line: CharSequence): CharSequence? {
        val original = line.toString()
        if (original.isBlank() || isMusicOnlyLine(original)) return null
        val removals = ArrayList<IntRange>()
        bracketPattern.findAll(original).forEach { match ->
            val body = match.groups[1]?.value ?: match.groups[2]?.value.orEmpty()
            if (isSoundAnnotation(body) || isBracketSpeakerLabel(body)) {
                removals += extendLeadingSeparator(original, match.range)
            }
        }
        parentheticalPattern.findAll(original).forEach { match ->
            val body = normalize(match.groups[1]?.value.orEmpty())
            if (!insideSpeakerPrefix(original, match.range) && isParentheticalAnnotation(body)) {
                removals += extendLeadingSeparator(original, match.range)
            }
        }
        pairedNotesPattern.findAll(original).forEach { match ->
            if (isMusicDescription(match.groups[1]?.value.orEmpty())) removals += match.range
        }

        var current = removeRanges(line, removals)
        var modified = removals.isNotEmpty()
        if (modified) current = normalizeSpacing(current)
        val withoutSpeaker = removeSpeakerPrefix(current) ?: return null
        if (withoutSpeaker.toString() != current.toString()) {
            current = normalizeSpacing(withoutSpeaker)
            modified = true
        }
        val visible = current.toString().trim()
        if (visible.isEmpty() || visible in dialogueMarkers || current.all { it.isWhitespace() || it.isMusicNote() }) return null
        return if (!modified && current.toString() == original) line else current
    }

    private fun isSoundAnnotation(body: String): Boolean {
        val normalized = normalize(body)
        if (normalized.isEmpty() || normalized.length > 80 || negativePattern.containsMatchIn(normalized)) return false
        if (noDialoguePattern.matches(normalized) || isMusicDescription(normalized)) return true
        if (normalized.any { it == '.' || it == '?' || it == '!' }) return false
        if (dialogueWordPattern.containsMatchIn(normalized) || normalized.split(' ').size > 8) return false
        return soundWordPattern.containsMatchIn(normalized) ||
            (soundSubjectPattern.containsMatchIn(normalized) && actionPattern.containsMatchIn(normalized))
    }

    private fun isParentheticalAnnotation(body: String) =
        directParentheticalPattern.matches(body) || languageDeliveryPattern.matches(body) ||
            deviceDeliveryPattern.matches(body) || deliveryPattern.matches(body) || isMusicDescription(body)

    private fun isMusicDescription(body: String): Boolean {
        val normalized = normalize(body).trim { it.isMusicNote() || it.isWhitespace() }
        if (normalized.isEmpty()) return true
        if (dialogueWordPattern.containsMatchIn(normalized)) return false
        val hasMusicWord = musicWordPattern.containsMatchIn(normalized)
        return hasMusicWord && (normalized == "music" || normalized == "instrumental" ||
            actionPattern.containsMatchIn(normalized) || musicMoodPattern.containsMatchIn(normalized) ||
            normalized.split(' ').size <= 3)
    }

    private fun isMusicOnlyLine(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || (!trimmed.first().isMusicNote() && !trimmed.last().isMusicNote())) return false
        return isMusicDescription(trimmed.trim { it.isMusicNote() || it.isWhitespace() })
    }

    private fun removeSpeakerPrefix(line: CharSequence): CharSequence? {
        val plain = line.toString()
        val match = speakerPrefixPattern.find(plain) ?: return line
        val label = match.groups[2]?.value ?: return line
        val colon = match.range.first + match.value.lastIndexOf(':')
        val before = plain.getOrNull(colon - 1)
        val after = plain.drop(colon + 1).firstOrNull { !it.isWhitespace() }
        if (before?.isDigit() == true && after?.isDigit() == true) return line
        if (!isLikelySpeakerLabel(label)) return line
        if (plain.substring(match.range.last + 1).isBlank()) return null
        val start = match.groups[2]?.range?.first ?: return line
        return removeRanges(line, listOf(start..match.range.last))
    }

    private fun isLikelySpeakerLabel(raw: String): Boolean {
        val label = raw.replace(parentheticalQualifierPattern, "").trim()
        if (label.isEmpty() || label.length > 32 || "://" in label || label.lowercase() in speakerBlocklist) return false
        if (label.any(Char::isDigit) && label.any(Char::isLowerCase)) return false
        if (speakerRolePattern.matches(label)) return label.firstOrNull()?.isUpperCase() == true
        val letters = label.filter(Char::isLetter)
        if (letters.isEmpty()) return false
        if (letters.all(Char::isUpperCase)) return true
        val words = label.split(Regex("""\s+""")).filter(String::isNotBlank)
        return words.size in 1..3 && words.all { word ->
            word.split('-', '\'', '’').filter(String::isNotBlank).all { part ->
                val chars = part.filter(Char::isLetter)
                chars.isNotEmpty() && (chars.all(Char::isUpperCase) ||
                    (chars.first().isUpperCase() && chars.drop(1).all(Char::isLowerCase)))
            }
        }
    }

    private fun isBracketSpeakerLabel(raw: String): Boolean {
        val label = raw.replace(parentheticalQualifierPattern, "").trim()
        return speakerRolePattern.matches(label) || isLikelySpeakerLabel(label)
    }

    private fun insideSpeakerPrefix(line: String, range: IntRange): Boolean {
        val colon = line.indexOf(':', range.last + 1)
        if (colon <= range.last || colon > 48) return false
        return isLikelySpeakerLabel(line.substring(0, colon).trim().removePrefix("-").removePrefix("–").removePrefix("—").trim())
    }

    private fun extendLeadingSeparator(line: String, range: IntRange): IntRange {
        val prefix = line.substring(0, range.first).trim()
        if (prefix.isNotEmpty() && prefix !in dialogueMarkers) return range
        var cursor = range.last + 1
        while (cursor < line.length && line[cursor].isHorizontalWhitespace()) cursor++
        if (line.getOrNull(cursor) != ':' && line.getOrNull(cursor) != ';') return range
        cursor++
        while (cursor < line.length && line[cursor].isHorizontalWhitespace()) cursor++
        return range.first until cursor
    }

    private fun normalize(value: String) = whitespacePattern.replace(value.trim().lowercase(), " ")

    private fun removeRanges(source: CharSequence, ranges: List<IntRange>): CharSequence {
        if (ranges.isEmpty() || source.isEmpty()) return source
        val keep = BooleanArray(source.length) { true }
        ranges.forEach { range ->
            val start = range.first.coerceIn(0, source.length)
            val end = (range.last + 1).coerceIn(start, source.length)
            for (index in start until end) keep[index] = false
        }
        return buildFromMask(source, keep)
    }

    private fun normalizeSpacing(source: CharSequence): CharSequence {
        if (source.isEmpty()) return source
        val keep = BooleanArray(source.length) { true }
        var start = 0
        while (start < source.length && source[start].isWhitespace()) keep[start++] = false
        var end = source.length - 1
        while (end >= start && source[end].isWhitespace()) keep[end--] = false
        var previousSpace = false
        for (index in start..end) {
            val space = source[index].isHorizontalWhitespace()
            if (space && previousSpace) keep[index] = false
            previousSpace = space
        }
        return buildFromMask(source, keep)
    }

    private fun buildFromMask(source: CharSequence, keep: BooleanArray): CharSequence {
        if (keep.all { it }) return source
        fun appendRanges(append: (Int, Int) -> Unit) {
            var index = 0
            while (index < source.length) {
                while (index < source.length && !keep[index]) index++
                val start = index
                while (index < source.length && keep[index]) index++
                if (start < index) append(start, index)
            }
        }
        return if (source is Spanned) SpannableStringBuilder().also { out ->
            appendRanges { start, end -> out.append(source, start, end) }
        } else buildString { appendRanges { start, end -> append(source, start, end) } }
    }

    private fun splitLines(text: CharSequence): List<CharSequence> {
        val lines = ArrayList<CharSequence>()
        var start = 0
        var index = 0
        while (index < text.length) {
            if (text[index] == '\n' || text[index] == '\r') {
                lines += text.subSequence(start, index)
                if (text[index] == '\r' && index + 1 < text.length && text[index + 1] == '\n') index++
                start = index + 1
            }
            index++
        }
        lines += text.subSequence(start, text.length)
        return lines
    }

    private fun joinLines(lines: List<CharSequence>, spans: Boolean): CharSequence {
        if (!spans) return lines.joinToString("\n")
        return SpannableStringBuilder().also { out ->
            lines.forEachIndexed { index, line ->
                if (index > 0) out.append('\n')
                out.append(line)
            }
        }
    }

    private fun Char.isMusicNote() = this == '♪' || this == '♫'
    private fun Char.isHorizontalWhitespace() = this == ' ' || this == '\t'
}
