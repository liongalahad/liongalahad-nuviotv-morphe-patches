package io.github.liongalahad.nuviotv.patches.subtitles.sdh

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import io.github.liongalahad.nuviotv.patches.settings.hub.settingsHubPatch
import io.github.liongalahad.nuviotv.patches.shared.Constants.NUVIO_COMPATIBILITY

private const val SETTINGS_RUNTIME =
    "Lio/github/liongalahad/nuviotv/extension/settings/MorpheSettingsRuntime;"
private const val CUE_TRANSFORMER =
    "Lio/github/liongalahad/nuviotv/extension/subtitles/sdh/SdhCueTransformer;"

@Suppress("unused")
val removeSdhAnnotationsPatch = bytecodePatch(
    name = "Remove SDH Annotations",
    description = "Adds Settings → Morphe → Subtitles and removes SDH annotations from Media3 subtitles when enabled.",
    default = false
) {
    compatibleWith(NUVIO_COMPATIBILITY)
    dependsOn(settingsHubPatch)
    extendWith("extensions/nuviotv.mpe")

    execute {
        SettingsScreenFingerprint.method.apply {
            val ordinalCallIndex = SettingsScreenFingerprint.instructionMatches.last().index
            val resultInstruction = getInstruction<OneRegisterInstruction>(ordinalCallIndex + 1)
            val register = resultInstruction.registerA
            addInstructions(
                ordinalCallIndex + 2,
                """
                    invoke-static/range { v$register .. v$register }, $SETTINGS_RUNTIME->mapVisibilityOrdinal(I)I
                    move-result v$register
                """
            )
        }

        SettingsCategoryClickFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static/range { p1 .. p1 }, $SETTINGS_RUNTIME->openIfMorphe(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :original
                sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                return-object v0
            """,
            ExternalLabel("original", SettingsCategoryClickFingerprint.method.getInstruction(0))
        )

        fun MutableMethod.hook(parameter: String, returnType: String) = addInstructions(
            0,
            """
                invoke-static/range { p1 .. p1 }, $CUE_TRANSFORMER->clean($parameter)$returnType
                move-result-object p1
            """
        )

        CueGroupOutputFingerprint.method.hook(
            "Landroidx/media3/common/text/CueGroup;",
            "Landroidx/media3/common/text/CueGroup;"
        )
        LegacyCueOutputFingerprint.method.hook("Ljava/util/List;", "Ljava/util/List;")
    }
}
