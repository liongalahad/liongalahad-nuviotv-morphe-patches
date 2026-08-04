# Remove SDH Annotations

Patch ID: `sdh-annotations`. Target: official NuvioTV `0.8.1-beta`, Media3/ExoPlayer only.

The patch adds `Settings → Morphe → Subtitles → Remove SDH Annotations`. It is OFF by default and stores its Boolean in private `morphe_patches` preferences under `subtitles.remove_sdh_annotations`.

Run `./scripts/test.ps1 -Patch sdh-annotations -Device tv`. Evidence is written below `local/patches/sdh-annotations/<timestamp>/` and is never committed.
