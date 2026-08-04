# Test plan

1. Run extension unit tests and build the bundle.
2. Apply only `Remove SDH Annotations` to the hash-pinned x86_64 APK.
3. Confirm every structural fingerprint matches exactly once.
4. Inspect the patched manifest for the activity and metadata, and DEX for the injected runtime and transformer.
5. On `Television_4K`, execute every acceptance item in `patch.json`, including D-pad navigation and Media3 playback.
6. On `Pixel_10`, validate Manager private-source import and exclusive selection using a user-provided PAT.
7. Before stable release, repeat installation, persistence, playback, and SDH behavior on a real Android TV and commit a report under `validation/`.

MPV/libass is outside this patch's compatibility claim.
