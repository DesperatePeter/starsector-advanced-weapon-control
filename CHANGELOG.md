# Changelog

Public-facing release history for Advanced Gunnery Control.

This file tracks curated public releases. Historical upstream notes remain in
`changelog.txt` because the Starsector version checker currently reads that
file.

## 2.0.0 - 2026-05-12

- Updated Advanced Gunnery Control for Starsector `0.98a`.
- Added optional weapon-type ignore controls for shield, armor, phase, and
  selected priority tags, including kinetic, high explosive, fragmentation,
  energy, beam, missile, and projectile weapons. Energy, missile, and
  projectile controls are hidden and inactive unless enabled in settings.
- Added Default buttons to editable tag and ship-mode modals for resetting
  parameters without changing the edited item.
- Added a compatibility fallback for old DesperatePeter AGC external Global
  weapon-composition tag exports.
- Restored ship-mode controls in simple mode with a smaller Novice ship-mode
  list.
- Fixed custom tag/ship mode modals suppressing background option hover while
  keeping real modal tooltips available.
- Hardened synchronized-fire and focused-target priority state against malformed
  combat cache data, and tightened campaign editor panels so they do not grow
  beyond their parent bounds.
