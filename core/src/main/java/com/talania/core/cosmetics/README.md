# Cosmetics

## Purpose

Core cosmetics runtime that:
- Captures a player’s base cosmetics on login.
- Rebuilds the model using **base set → race overrides**.
- Applies debug toggles for hide/strip base and view mode.

## Runtime Flow

1. On first access, capture the current `PlayerSkin` as the base.
2. Rebuild the `ModelComponent` using:
   - Base attachments (unless `debugHideBase` is enabled).
   - Race cosmetics/overrides.
3. If `debugStripBase` is enabled, a stripped base skin is used.
4. If `debugHideBase` is enabled (without strip), the base model/texture is swapped to `Characters/Empty_Cube.*`.

## Debug Behavior

- `debugHideBase`: Hides the base model (keeps cosmetics).
- `debugStripBase`: Strips base cosmetics (keeps skin data cached).
- Offsets are currently **disabled** (no runtime patching of models).

## Assets

Cosmetic assets are not committed to git. They are packaged locally during build via the `.local` asset pipeline and live under:
- `Common/Characters/Body_Attachments/...`
- `Common/Characters/Player_Textures/...`

## Key Files

- `TalaniaCosmeticCore.java`
- `TalaniaCosmetics.java`

