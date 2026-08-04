package io.github.liongalahad.nuviotv.patches.subtitles.sdh

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

private const val CUE_GROUP = "Landroidx/media3/common/text/CueGroup;"
private const val TEXT_OUTPUT = "Landroidx/media3/exoplayer/text/TextOutput;"

/** Exact 0.8.1-beta settings Composable and its visibility-filter ordinal call. */
internal object SettingsScreenFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        literal(0x7f1108b9),
        literal(0x7f1108ba),
        literal(0x7f1108b7),
        literal(0x7f1108b8),
        methodCall(
            definingClass = "Lkotlin/collections/CollectionsKt;",
            name = "listOf",
            returnType = "Ljava/util/List;"
        ),
        methodCall(name = "ordinal", returnType = "I")
    )
)

/** Generated section-click lambda, matched by state-selection behavior. */
internal object SettingsCategoryClickFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        string("category"),
        methodCall(name = "getValue", returnType = "Ljava/lang/Object;"),
        methodCall(name = "setValue", returnType = "V")
    )
)

/** Nuvio's CueNormalizingTextOutput Media3 callback. */
internal object CueGroupOutputFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf(CUE_GROUP),
    filters = listOf(
        methodCall(
            definingClass = CUE_GROUP,
            name = "<init>",
            parameters = listOf("Ljava/util/List;", "J")
        ),
        methodCall(
            definingClass = TEXT_OUTPUT,
            name = "onCues",
            parameters = listOf(CUE_GROUP),
            returnType = "V"
        )
    ),
    custom = { _, classDef -> TEXT_OUTPUT in classDef.interfaces }
)

internal object LegacyCueOutputFingerprint : Fingerprint(
    classFingerprint = CueGroupOutputFingerprint,
    returnType = "V",
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        methodCall(
            definingClass = TEXT_OUTPUT,
            name = "onCues",
            parameters = listOf("Ljava/util/List;"),
            returnType = "V"
        )
    )
)
