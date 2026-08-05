package io.github.liongalahad.nuviotv.patches.subtitles.sdh

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import io.github.liongalahad.nuviotv.patches.settings.hub.settingsUiPatch
import io.github.liongalahad.nuviotv.patches.shared.Constants.NUVIO_COMPATIBILITY
import org.w3c.dom.Element

private const val CUE_TRANSFORMER =
    "Lio/github/liongalahad/nuviotv/extension/subtitles/sdh/SdhCueTransformer;"
private const val CATEGORY_METADATA =
    "io.github.liongalahad.nuviotv.settings.category.subtitles"

private val sdhCategoryResourcePatch = resourcePatch {
    compatibleWith(NUVIO_COMPATIBILITY)
    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as Element
            application.appendChild(document.createElement("meta-data").apply {
                setAttribute("android:name", CATEGORY_METADATA)
                setAttribute("android:value", "true")
            })
        }
    }
}

@Suppress("unused")
val removeSdhAnnotationsPatch = bytecodePatch(
    name = "Remove SDH Annotations",
    description = "Adds Settings → Morphe → Subtitles and removes SDH annotations from Media3 subtitles when enabled.",
    default = false
) {
    compatibleWith(NUVIO_COMPATIBILITY)
    dependsOn(settingsUiPatch, sdhCategoryResourcePatch)
    extendWith("extensions/nuviotv.mpe")

    execute {
        listOf(CueGroupOutputFingerprint, LegacyCueOutputFingerprint).forEach {
            it.matchAll(1..1)
        }

        fun MutableMethod.hook(parameter: String, returnType: String) = addInstructions(
            0,
            """
                invoke-static/range { p1 .. p1 }, $CUE_TRANSFORMER->clean($parameter)$returnType
                move-result-object p1
            """
        )

        CueGroupOutputFingerprint.method.apply {
            val constructorIndex = CueGroupOutputFingerprint.instructionMatches.first().index
            val constructorInstruction = getInstruction<com.android.tools.smali.dexlib2.iface.instruction.Instruction>(
                constructorIndex
            )
            val listRegister = when (constructorInstruction) {
                is FiveRegisterInstruction -> constructorInstruction.registerD
                is RegisterRangeInstruction -> constructorInstruction.startRegister + 1
                else -> error("CueGroup constructor call uses an unsupported instruction format")
            }
            addInstructions(
                constructorIndex,
                """
                    invoke-static { v$listRegister }, $CUE_TRANSFORMER->clean(Ljava/util/List;)Ljava/util/List;
                    move-result-object v$listRegister
                """
            )
        }
        LegacyCueOutputFingerprint.method.hook("Ljava/util/List;", "Ljava/util/List;")
    }
}
