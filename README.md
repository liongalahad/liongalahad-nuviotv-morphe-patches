# NuvioTV Patches

Private Morphe patch source for the official NuvioTV Android application. The first patch is `Remove SDH Annotations`, supported only for `com.nuvio.tv` `0.8.1-beta` on the Media3/ExoPlayer subtitle path.

This repository distributes patch code and `.mpp` bundles. It never distributes original, patched, or modified NuvioTV APKs.

## Install as a private source

1. Create a GitHub fine-grained PAT that can read this private repository. Do not commit or paste it into project files.
2. In Morphe Manager on `Pixel_10`, add `liongalahad/nuviotv-patches` as a private patch source and provide the PAT when Manager requests it.
3. Import the official NuvioTV `0.8.1-beta` APK for the target ABI.
4. Select only `Remove SDH Annotations`. Use the dedicated local test signing identity for repeatable test upgrades.
5. Install on the dedicated TV test profile. The official app cannot be upgraded in place because the patched APK has a different signature.

Deep link: `https://morphe.software/add-source?github=liongalahad/nuviotv-patches`

## Local workflow

```powershell
.\scripts\bootstrap.ps1 -InstallMissing
.\scripts\build.ps1 -Patch sdh-annotations
.\scripts\patch.ps1 -Patch sdh-annotations
.\scripts\test.ps1 -Patch sdh-annotations -Device tv
.\scripts\verify-all.ps1
```

Every run is isolated under `local/patches/<patch-id>/<timestamp>/`. See [Windows setup](docs/SETUP_WINDOWS.md), [architecture](docs/ARCHITECTURE.md), [testing](docs/TESTING.md), and [release rules](docs/RELEASING.md).

## Available patches

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.2](https://github.com/liongalahad/nuviotv-patches/releases/tag/v1.0.0-dev.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 NuvioTV&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 0.8.1-beta |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove SDH Annotations](#remove-sdh-annotations) | Adds Settings → Morphe → Subtitles and removes SDH annotations from Media3 subtitles when enabled. |  |

</details>

<!-- PATCHES_END -->

## Branches

- `dev`: development and private prereleases.
- `main`: reviewed stable releases only. Merge `dev` without squashing so semantic-release sees the conventional commits.

License: GPL-3.0 with the template's `NOTICE` terms.
