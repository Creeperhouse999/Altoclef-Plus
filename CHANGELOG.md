# Changelog

All notable changes to AltoClef Plus are listed here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/). Each release on the
[Releases page](https://github.com/Creeperhouse999/Altoclef-Plus/releases) uses the matching section below
as its notes.

## [Unreleased]

### Added
- **Jump critical hits** in every fight: the bot waits for a full charge bar, jumps and hits on the way down
  (only where that's safe: not in water or lava, on ladders, in boats or under a low ceiling).
- **Axe or sword by situation**: an axe against shields and when one axe hit finishes the mob, a sword for
  groups (sweeping), otherwise whatever does the most damage per second. Netherite now counts too.
- **Avoid water in caves** (on by default): paths through cave water cost much more, so the bot goes around
  instead of wading and drowning. Lakes, rivers and oceans under open sky are not affected.
- **Getting air when drowning**: low on air underwater, the bot swims straight up if the surface is close;
  under a cave ceiling it places a door next to itself (doors can't hold water) and breathes inside it.
- **Carries a door** for those air pockets, made only from wood it already has.
- **Visible inventory** (on by default): when the bot crafts in its 2x2 grid, sorts or equips, the inventory
  screen opens so you can watch it click, at a slower human-like pace, and closes when it's done.
- **Your inventory is respected**: open it while the bot runs and the bot waits (no clicks, no walking,
  nothing closes it); close it and the bot carries on.

### Changed
- The HUD task line is now our own plain-English line: what you asked for and what the bot is doing right
  now, e.g. *Get iron pickaxe: Mining iron ore* or *Get spruce log x40: Picking up items*. AltoClef's own
  top task name often stayed the same for minutes (a crafting table while it was really mining iron).
- The Food / Pearls / Rods / Eyes / Beds line only shows while *Beat the game* (`@gamer`) runs.
- Pressing the pause key with nothing running now says *Nothing to pause*.

### Fixed
- Safe zone rules (no breaking, no placing, no entering) now keep working after the first task: AltoClef reset
  Baritone's rule lists whenever a task started, which silently dropped them.
- Presets no longer "finish" with items missing: at the end every step is checked again, and a step whose
  item was lost meanwhile (for example the chestplate after a death) is done again (at most 2 extra rounds).
- Armor made by a preset is put on at the end instead of staying in the inventory.

## [0.7.0] - 2026-10-15

First round of fixes from a real in-game test session.

### Added
- **Defend myself when idle** (on by default): when no task is running and nobody is at the keys, the bot
  now fights or runs from nearby mobs (zombies, creepers...) instead of standing still until it dies. It hands
  the controls back as soon as it is calm again or you touch a movement key. The HUD shows *[Defending]*.
- **Steady mining** (on by default): the bot keeps going for the block it already chose instead of switching
  targets several times a second. A new block only wins when it is much closer, at most once every 3 seconds.

### Fixed
- After dying, the bot again goes back for its items: deaths while it was idle no longer count towards the
  "keeps dying here, don't go back" guard.
- Pausing the game (Esc menu) no longer makes stuck recovery think the bot is stuck when you come back.
- "Putting away a stuck cursor item" no longer interrupts furnace and crafting-table work; it now waits much
  longer while a container is open (and a bit longer everywhere).

## [0.6.1] - 2026-10-15

### Changed
- New icon: a pixel-art diamond pickaxe with a green "+" badge.
- Releases are now also published on Modrinth.

## [0.6.0] - 2026-10-15

Safe zones, safety rules and many more settings. Built and unit-tested; please report anything odd you see
in-game.

### Added
- **Safe zones** (new *Zones* tab): name a zone, stand on two corners and press *Corner 1* / *Corner 2*,
  choose full or exact height, and create it. Each zone has its own rules: no breaking, no placing, no
  looting, no entering, and no attacking hostile, neutral or friendly mobs. New zones protect blocks, chests
  and friendly/neutral mobs; hostile mobs and walking through are allowed until you switch them off.
  Zones are saved per world, Baritone plans its paths around them, and *Show safe zones in the world* draws
  their outlines.
- **Safety rules**: never attack named mobs or tamed pets (on by default), a never-attack mob list, a
  never-break block list, and self-defense inside zones (the bot may still hit a mob that attacks it —
  never named or tamed ones).
- **Basics** in Settings: chest looting (on by default), sleep at night, loot ruined portals / desert
  temples, get pearls by piglin trading instead of hunting, eyes of ender to collect.
- **Gear up for** setting: gear gates now run only for tasks that need them by default — `@gamer`, deep
  ores (iron kit), Nether items (Nether gear) and End items (everything). Errands like "get dirt" start
  right away. Choose *@gamer only* or *Every task* if you prefer.

### Fixed
- The bot getting stuck in front of doors: diagonal and step up/down moves no longer try to walk through
  closed doors and fence gates, and the bot no longer starts eating while aiming at a door (which blocked
  the door click and toggled it open and shut). New setting: *Door fixes*.
- Camera: switching perspective (F5) snaps instead of swinging, and the view is never smoothed while
  spectating another entity. Works in first person and both third-person views.

## [0.5.1] - 2026-10-15

Safety fixes from a full code review, before wider in-game testing.

### Fixed
- The shield could use the main-hand item (place lava, throw pearls, light fires, open doors, mount boats).
  It is now only raised with a sword or empty hand, aiming at nothing or at a hostile mob.
- Stop and Pause now also stop the remaining commands of a preset (e.g. "Iron gear").
- The "put away cursor item" job could hold the bot for minutes; it now ends as soon as the cursor is empty.
- Endless golden-boots swap during `@gamer`: worn golden boots are never taken off.
- Resting to heal could repeat forever without food; it now only rests when it can heal and backs off after
  a rest that didn't help.
- The water-bucket gear gate no longer interrupts AltoClef while it pours water to make obsidian.
- If a boat crossing times out, the bot gets out of the boat.
- Side jobs no longer start while the bot is idle, time out per job, and a job that timed out waits before
  being tried again. Death recovery only runs when the bot (not you) was playing.
- Panel, Stop and Pause keys keep working even if the UI module is turned off.
- Broken or partial settings files (empty list entries, odd numbers) can no longer crash the game.

### Changed
- Pause / resume key is now **O** (P is vanilla's Social Interactions key).

## [0.5.0] - 2026-10-15

Travel and pearls. Built and unit-tested; please report anything odd you see in-game.

### Added
- Enderman hunting with a boat trap: in a warped forest the bot puts a boat down, stares at an enderman so
  it comes over, and once it sits in the boat (it can't teleport out) kills it with jump critical hits. If
  the trap doesn't work in 20 seconds, or there's no boat, it fights with crits anyway. Nether wood can't
  make boats, so the bot brings one from the Overworld. Also a *Hunt endermen* button in the panel.
- Boats across oceans and big lakes: when 24+ blocks of open water lie straight ahead on a long trip, the
  bot crosses by boat and picks the boat back up on the other side. Rivers and ponds are ignored.
- Safety stashes (each optional, once per run): set spawn with a bed by the Nether portal (on by default),
  leave valuables in a chest before the Nether, leave a spare iron kit in a chest before the Nether or
  the End.

### Fixed
- Stronghold estimates like "y = 1620": the eye-of-ender throws are now intersected on the ground plane,
  unreliable throws are rejected and re-thrown, and the second throw is farther to the side.
- Nether back-and-forth on the way to the stronghold: walking vs. the Nether is decided once per trip,
  and obsidian plus flint and steel are collected *before* entering the Nether.
- Lava "crosses" in the Nether: AltoClef tried to make obsidian with water in the Nether (water
  evaporates) — it now only does that in the Overworld.
- Exact Nether coordinates for negative positions (they were one block off).

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

[Unreleased]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.6.1...HEAD
[0.6.1]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.6.0...v0.6.1
[0.6.0]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.5.1...v0.6.0
[0.5.1]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.5.0...v0.5.1
[0.5.0]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Creeperhouse999/Altoclef-Plus/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Creeperhouse999/Altoclef-Plus/releases/tag/v0.1.0
