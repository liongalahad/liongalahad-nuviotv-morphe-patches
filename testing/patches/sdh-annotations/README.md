# Remove SDH Annotations

Patch ID: `sdh-annotations`. Target: official NuvioTV `0.8.1-beta`, Media3/ExoPlayer only.

The patch adds `Settings → Morphe → Subtitles → Remove SDH Annotations`. It is OFF by default and stores its Boolean in private `morphe_patches` preferences under `subtitles.remove_sdh_annotations`.

When enabled, the Media3 hook processes every outgoing text-cue list, applies the original cleaner's aggressive `FULL` behavior, suppresses cues that become empty, and preserves timing, cue layout, surviving spans, and non-text cues. It does not modify subtitle files, Nuvio account data, playback reports, or backend traffic. MPV/libass subtitles are not processed.

See [the exact SDH removal rules and known limitations](../../../docs/SDH_REMOVAL.md). Every supported complete bracketed or parenthetical block is removed regardless of content, so ordinary bracketed/parenthetical dialogue can also be removed while the switch is ON.

Run `./scripts/test.ps1 -Patch sdh-annotations -Device tv`. Evidence is written below `local/patches/sdh-annotations/<timestamp>/` and is never committed.
