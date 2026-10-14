# Changelog

All notable changes to AltoClef Plus are listed here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/). Each release on the
[Releases page](https://github.com/Creeperhouse999/Altoclef-Plus/releases) uses the matching section below
as its notes.

## [Unreleased]

## [0.4.0] - 2026-10-14

Stuck recovery and getting items back after dying. Built and unit-tested; please report anything odd you
see in-game.

### Added
- Stuck detection while a task runs: no progress, endless pillaring (including the Nether ceiling and
  bobbing up and down), breaking the same block over and over, pacing back and forth, a task restarting
  again and again, and Nether back-and-forth. Menus, eating, fights, smelting and AltoClef Plus side jobs
  never count as stuck.
- Step-by-step recovery: new path → skip that block or spot (or climb back down) → restart the task →
  walk away and try again, with longer pauses each round.
- Safe mode after several failed rounds: leaves caves, rests and heals, then tries again. The bot is only
  stopped if you turn on *Stop bot after safe modes*.
- Nether back-and-forth turns off Nether fast travel for the rest of the session, so the bot walks.
- Death recovery: goes back for its items before they despawn (quick wooden tools first when the spot is
  far or deep). Skips lava and void deaths and places where it keeps dying.
- Every recovery action shows in the HUD and the log.

## [0.3.0] - 2026-10-14

Safer progression: the bot gears up before taking risks instead of rushing and dying.
Built and unit-tested; please report anything odd you see in-game.

### Added
- Gear gates (each can be switched off):
  - stone sword, axe and shovel as soon as it has a stone pickaxe — no more mining iron without a sword;
  - iron sword, shield and water bucket after the iron pickaxe;
  - full iron armor before going diamond hunting;
  - armor, shield and food before entering the Nether.
  A gate that can't be completed is paused for a while, so the bot never gets stuck on it.
- Caution: rests and eats when hurt, runs from mobs when badly hurt, and doesn't keep digging deeper
  while weak.
- Shield: moved to the offhand and raised against arrows, fireballs, aiming skeletons/blazes and fusing
  creepers. Only while the bot is running, so it never takes over your right-click.
- Projectile dodging without a shield: sidesteps, but only onto solid ground with no lava.
- Water bucket clutch: AltoClef's clutch is kept on and the water bucket has its own hotbar slot.
- No AltoClef Plus side job can hold the bot for more than 5 minutes.

## [0.2.0] - 2026-10-14

Smarter inventory. Built and unit-tested; please report anything odd you see in-game.

### Added
- Wears better armor as soon as it has it. Golden boots stay on in the Nether (piglins).
- Crafts armor piece by piece as soon as leftover iron or diamonds allow, after keeping enough for the
  pickaxe, sword, shield and bucket.
- Keeps a cheap spare axe and shovel (wood or stone) and replaces them before they break — only from
  materials it already carries.
- Drops junk when the inventory is nearly full: only items on the junk list (gunpowder, rotten flesh,
  seeds, ...) and extra stacks of cobblestone/dirt-like blocks. Never tools, armor, named or enchanted
  items, protected items, or anything the current task needs.
- Puts away items that get stuck on the cursor.

### Fixed
- Hotbar items bouncing between hand and inventory forever (AltoClef and Baritone fought over the same
  slot). Items already on the hotbar are now just selected; the default layout matches Baritone's slots.
- Mining logs and dirt with a helmet or crafting table in hand: the best tool is now held on every
  block-break tick.
- Eye of ender crafting looping forever when the recipe book placed the ingredients.
- Tunnelling hundreds of blocks back to an old crafting table (`@gamer` now only goes back for tables
  within the configured distance).
- Walking to far-away furnaces instead of placing a new one.
- Blaze powder could be thrown away during `@gamer`.

## [0.1.0] - 2026-10-14

First preview. Built and unit-tested; not yet widely tested in-game — please report anything odd.

### Added
- Project setup: Fabric mod for Minecraft 1.18.2 that loads on top of AltoClef 0.4.1 and Baritone.
- The build downloads the official AltoClef v0.4.1 release automatically and verifies its checksum.
- Settings file `.minecraft/altoclef/altoclefplus.json`, created with defaults on first launch. Broken or
  partial files fall back to defaults instead of crashing.
- Start / stop / pause / resume control for the bot. Pause remembers the running command and resumes it.
- AltoClef `@` and Baritone `#` chat commands are blocked (the control panel replaces them); this can be
  switched off in settings.
- AltoClef and Baritone chat messages go to an in-game log instead of flooding chat.
- AltoClef's own task overlay text is hidden (replaced by the AltoClef Plus HUD).
- `@gamer` progress roadmap: 19 milestones from "Wood" to "Dragon killed", ticked from your inventory and
  world state.
- Step history: the list of steps the bot worked on and how long each took.
- Mod Menu support: icon, description and links in the mod list (optional dependency).
- Bug report and feature request forms; automatic builds, and releases built from tags.
- Control panel (Right Shift) with Tasks, Plan, Status and Settings tabs; searchable item grid; presets.
- HUD: state badge, current task, next milestones, alerts and key item counts.
- Stop (K) and Pause/Resume (P) keys, rebindable in Controls.
- Settings editors for protected items, junk items and hotbar layout; Mod Menu settings button.
- Smooth camera (experimental): the view turns smoothly while the bot is in control; the bot's real aim
  stays instant. Snaps back on teleports, dimension changes, open screens and when you take control.
- Fair combat: the bot no longer hits mobs through walls, and turns to face a mob before hitting it.

[Unreleased]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.4.0...HEAD
[0.4.0]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Creeperhouse999/Altoclef-Plus/releases/tag/v0.1.0
