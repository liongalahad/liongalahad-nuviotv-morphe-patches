# Architecture

## Compartments

```text
patches/.../nuviotv/<category>/<patch-id>/        patcher/fingerprints
extensions/.../nuviotv/<category>/<patch-id>/     injected runtime
testing/patches/<patch-id>/                        manifest, tests, porting notes
local/patches/<patch-id>/<timestamp>/              APKs/logs/evidence; gitignored
```

The repository's concrete package root is `io.github.liongalahad.nuviotv`; the logical `nuviotv/<category>/<patch-id>` boundary is retained inside both code modules.

## Shared settings hub

The internal settings-hub resource patch injects one initialization provider and one metadata registration. On 0.8.1-beta, Nuvio already compiles a hidden `EXPERIENCE` settings section. The patch changes its label to `Morphe`, maps that slot to a visible branch inside the exact settings filter, and converts its native mode card into an inline `Subtitles` compartment. Expanding that card draws `Remove SDH Annotations` with Nuvio's own Boolean setting renderer immediately below it. Navigation therefore remains inside Nuvio's existing Settings pane and inherits its typography, focus treatment, spacing, colors, and switch styling.

The switch action writes private `morphe_patches` storage synchronously and pulses the pane's captured Compose state back to its original value so the native row redraws immediately. Preferences never enter Nuvio profiles, account sync, telemetry, playback reports, or backend DTOs.

## SDH hook

The Media3 hook targets Nuvio's `TextOutput` wrapper structurally: `CueGroup` input, reconstruction with the original `presentationTimeUs`, and forwarding to `TextOutput`. Each outgoing group rechecks the current cached preference. Text cues are copied only when changed; timing, positioning, surviving spans, non-text cues, and styling are retained. Empty cleaned cues are suppressed.

MPV/libass is explicitly deferred.
