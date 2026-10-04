# Vanta Client 26.2 V3

Original Fabric client source targeting Minecraft 26.2 / Java 25.

## Controls
- **Right Shift** — Vanta module/settings GUI
- **Right Ctrl** — HUD editor
- **C** — hold Zoom
- **Left Alt** — hold Freelook

## V3 HUD system
Every HUD widget can be dragged and configured independently:
- Scale from **55% to 200%**
- Background **On / Off**
- Style presets: **Vanilla / Clean / Glass / Vanta**
- Saved positions and appearance
- Smooth movement option

HUD widgets include:
- FPS / Ping / Coordinates / Direction / CPS / RAM / Clock / Server / Player Count
- Food + saturation overlay (original implementation inspired by the same information goal as food/saturation HUD mods; no third-party code or textures copied)
- Armor HUD with real item icons and durability percentages
- Totem counter
- Status-effect bars with timers
- Keystrokes

## Client-side modules
- Auto Sprint
- Freelook (Left Alt)
- Zoom
- Crosshair
- Fullbright (uniform lightmap brightness)
- Night Vision (client-side gamma-style bright view)
- No Pumpkin Blur
- Low Fire
- Low Shield
- Item Scaling
- Boat Map Visibility
- Riptide Shield Fix
- Fog controls
- View Bobbing Off
- Entity Shadows Off
- Minimal Particles

## Performance
- Reversible FPS Boost preset
- Dynamic FPS when unfocused
- Render Distance Cap
- Entity Distance reduction
- Simulation/Chunk Performance cap
- Fast/Minimal particles

## Build
The included GitHub Actions workflow uses Java 25 and Gradle 9.7.1. On GitHub, open **Actions → Build Vanta Client**. The normal JAR (not the `-sources.jar`) goes in `.minecraft/mods` with Fabric API.

## Clean-room note
Vanta V3 is original code. The GUI is inspired by modern Minecraft client layouts, but does not copy Prestige/Lunar source code or assets. Third-party mod code/assets are not bundled or rebranded.
