# Test plan

1. Run extension unit tests covering the fresh normalize-only default, true-Off identity, normalize-only behavior, multiline delimiters, repeated inferred markers, retained spans, cleanup modes, and build the bundle.
2. Apply only `Remove SDH Annotations` independently to the hash-pinned x86_64 and universal APKs.
3. Confirm every structural fingerprint matches exactly once.
4. Inspect the patched manifest for the initialization provider and metadata, and DEX for the injected runtime, native Compose action, and transformer.
5. On `Television_4K`, install the universal-derived output and execute every acceptance item in `patch.json`, including an in-pane visual comparison with Layout/Playback, D-pad navigation, and Media3 playback using an emulator-compatible H.264 source.
6. Verify unknown boundary-token learning with a multiline first block and a one-line second consecutive block, three non-consecutive total blocks, mixed case, straight/curly quote variants, and all four cleanup modes.
7. On `Pixel_10`, validate public-source import, prerelease selection, and exclusive patch selection.
8. Before stable release, repeat installation, persistence, playback, and SDH behavior on a real Android TV and commit a report under `validation/`.

MPV/libass is outside this patch's compatibility claim.

## NuvioTV 0.8.5-beta automated port evidence

- [x] The 198-test extension suite passed with zero failures, errors, or skips.
- [x] This patch applied alone with an exact single match on x86_64, arm64-v8a, armeabi-v7a, and universal official APKs.
- [x] Each isolated output passed SHA-256 input verification plus post-patch manifest, injected-class, and forbidden-type inspection.
- [x] The combined ten-patch x86_64 and universal builds applied without a failed patch and passed APK signature and alignment verification.
- [x] The combined x86_64 side-by-side build installed and launched on `Television_4K`; its process remained alive with no startup `FATAL EXCEPTION`, `VerifyError`, or package ANR.
- [ ] Patch-specific D-pad, persistence, navigation, playback, and media behavior remains manual and is not marked passed by this automated port.
- [ ] Real Android TV acceptance remains pending.
