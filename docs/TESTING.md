# Testing

## Layers

1. Unit: extension cleaner/runtime behavior and Android span/Media3 object preservation.
2. Application: hash the official APK, build `.mpp`, select one patch in exclusive mode, and require all fingerprints to resolve once on both x86_64 and universal assets.
3. Inspection: verify activity and metadata in the manifest, injected classes, preference hook, and cue hook.
4. Phone Manager: add the public GitHub source on `Pixel_10`, select one patch, export using the dedicated test key policy, and install the result on the TV target.
5. TV AVD: install/launch on `Television_4K`; collect logcat, screenshot, UI hierarchy, device facts, patch result, input/bundle digests, and the manual checklist.
6. Real Android TV: install, D-pad navigate, check persistence through force-stop/reboot, run Media3 playback/seeking/track changes, and record exact device/version/ABI results.

## Commands

```powershell
.\scripts\build.ps1 -Patch sdh-annotations
.\scripts\patch.ps1 -Patch sdh-annotations
.\scripts\patch.ps1 -Patch sdh-annotations -Abi universal
.\scripts\test.ps1 -Patch sdh-annotations -Device tv
.\scripts\test.ps1 -Patch sdh-annotations -Device tv -Asset universal
.\scripts\test.ps1 -Patch sdh-annotations -Device phone
.\scripts\test.ps1 -Patch sdh-annotations -Device real -Serial <adb-serial>
.\scripts\verify-all.ps1
```

`test.ps1` automates preparation and evidence but leaves behavioral checkboxes unchecked. A human must verify actual subtitle rendering and D-pad navigation. Real-device uninstall is never implicit; `-ReplaceOfficial` is required when signatures conflict.

## SDH sample assertions

With the toggle OFF, use the exact source text. With it ON, verify sound/music/SDH blocks and speaker labels disappear while dialogue, ordinary parentheses, times, URLs, ratios, punctuation, cue timing/position, non-text cues, and spans survive. Repeat after seeking and changing subtitle tracks. Search logcat for crashes, ANRs, `VerifyError`, and fatal entries.
