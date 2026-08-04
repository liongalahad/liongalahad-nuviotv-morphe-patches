# Porting notes

Source reference: local `NuvioTV-0.8.0-sdh` worktree. Ported behavior includes bracketed and parenthetical annotations, speaker labels, music/sound suppression, whitespace cleanup, Android span preservation, empty-cue suppression, and `CueGroup` timing preservation.

Deliberately omitted: SDH audit logs, track logs, playback-report DTOs, backend/reporting changes, and Nuvio profile synchronization.

The 0.8.1-beta R8 target is matched through the Media3 interface/callback shape and the settings resource/section-building sequence. Never widen a failed fingerprint. A new Nuvio version remains unsupported until isolated patching and every runtime gate pass.
