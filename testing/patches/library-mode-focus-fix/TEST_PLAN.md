# Library Mode Focus Fix test plan

## Isolated

1. Apply only `Library Mode Focus Fix` to the official x86_64 APK.
2. Open Library with Saved selected.
3. Press Down once, then Up once; verify focus returns to Saved.
4. Select Cloud, press Down once, then Up once; verify focus returns to Cloud.
5. Verify no Morphe setting or manifest component is added by this patch.

## Combined

1. Apply the fix with Local Media and all other current optional patches.
2. Repeat the Saved and Cloud paths and verify neither returns to Storage.
3. Select Storage and verify its native Local Media navigation remains unchanged.
4. Check logs for crashes, verifier errors, and `MorpheLibraryFocus` errors.

## NuvioTV 0.8.5-beta automated port evidence

- [x] The 198-test extension suite passed with zero failures, errors, or skips.
- [x] This patch applied alone with an exact single match on x86_64, arm64-v8a, armeabi-v7a, and universal official APKs.
- [x] Each isolated output passed SHA-256 input verification plus post-patch manifest, injected-class, and forbidden-type inspection.
- [x] The combined ten-patch x86_64 and universal builds applied without a failed patch and passed APK signature and alignment verification.
- [x] The combined x86_64 side-by-side build installed and launched on `Television_4K`; its process remained alive with no startup `FATAL EXCEPTION`, `VerifyError`, or package ANR.
- [ ] Patch-specific D-pad, persistence, navigation, playback, and media behavior remains manual and is not marked passed by this automated port.
- [ ] Real Android TV acceptance remains pending.
