# Porting notes

Source reference: local `NuvioTV-0.8.0-sdh` worktree. Ported behavior includes bracketed and parenthetical annotations, speaker labels, music/sound suppression, whitespace cleanup, Android span preservation, empty-cue suppression, and `CueGroup` timing preservation.

The Boolean switch maps to the source implementation's `FULL` mode. While enabled, every complete supported square-bracket and parenthetical block is removed without vocabulary classification. Do not replace this with the source cleaner's conservative sound-description mode; doing so permits unknown, punctuated, and environmental SDH annotations to survive.

Deliberately omitted: SDH audit logs, track logs, playback-report DTOs, backend/reporting changes, and Nuvio profile synchronization.

The 0.8.1-beta R8 target is matched through the Media3 interface/callback shape and the settings resource/section-building sequence. Never widen a failed fingerprint. A new Nuvio version remains unsupported until isolated patching and every runtime gate pass.

Nuvio's hidden `EXPERIENCE` destination already stays inside the Classic/TV detail pane. The patch exposes that destination and structurally discovers Nuvio's native Boolean setting renderer from the forced-subtitles row. Do not restore a click interceptor or a separate Activity: both break visual and navigational parity with Layout and Playback.
