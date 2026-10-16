# Changelog

All notable changes to AltoClef Plus are listed here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/). Each release on the
[Releases page](https://github.com/Creeperhouse999/Altoclef-Plus/releases) uses the matching section below
as its notes.

## [Unreleased]

## [0.9.0] - 2026-10-16

New Presets tab with kits, and blueprint blocks for building. Both are unit-tested only so far.

### Added
- **Presets tab:** about 45 ready-made presets in groups (tools, armor, kits such as "Wood tools + leather
  armor", resources, food and base, speedrun) next to your own presets. Click to run; shift-click one of yours
  to delete it, ctrl-click to edit it.
- **Kit builder** (*Make a kit*): pick items from the item grid with an amount (+1 / +16 / +64 or typed), name
  the kit and save it as a preset, or run it right away. The bot gathers everything as one task. Items AltoClef
  can't get are refused with a message.
- **Blueprint blocks** in the build preview and while building, like Litematica: missing blocks are
  see-through blue cubes, a wrong block in their place is outlined red, placed blocks disappear. Setting:
  Overlay: blueprint blocks.
- **Unload** button in the Build tab: takes the selected schematic out of the world view.

## [0.8.4] - 2026-10-16

Fixes from a morning @gamer run to the Nether portal. Tested in-game: golden boots at the portal, the furnace
loop fix, throwing away old armor. Unit-tested only: eating while resting, the pillaring fix, flee cooldown.

### Changed
- @gamer wears full diamond (boots too) outside the Nether. Golden boots are fetched before the Nether, put on
  next to the portal and in the Nether, and thrown away once the bot is back out with ender eyes.

### Fixed
- The furnace opened and closed forever while crafting flint and steel (AltoClef read the furnace's output slot
  as the crafting grid).
- Chest take rules ("leave at least ...") no longer apply to furnaces.
- Junk now includes armor no better than what is worn, more than two of the best tool, and stones and ores
  @gamer never uses (calcite, raw copper, amethyst, ...), so the inventory doesn't fill up.
- Resting eats to full so health can regenerate (it rested at 14 hp for an hour without healing).
- Undoing a useless pillar no longer digs straight down underground (it dug into lava).
- Running from mobs restarts at most every 5 seconds; the message says why (pillager, outnumbered, still chased).

## [0.8.3] - 2026-10-16

Fixes from an overnight @gamer run in a world with pillagers all around spawn. Tested in-game: the night
shelter, avoiding pillagers after a death, running from pillagers without iron armor (the bot then got
out of the area and made iron gear with no more deaths), Plus messages in the game log.

### Added
- Night shelter in @gamer: without iron armor and without a bed, the bot digs three blocks down, covers
  the hole and waits for dawn instead of dying to the first night's mobs. It never digs into caves,
  liquids or bedrock. Setting: Survival → "Hide in a hole at night without armor (@gamer)", on by default.
- Without an iron (or better) chestplate, a pillager within 24 blocks makes the bot run, like being
  outnumbered: crossbows outrange a sword.
- AltoClef Plus messages are also written to the game log (latest.log).

### Fixed
- Dying with pillagers nearby now marks a 48-block area to avoid for 10 minutes after one death.
- "Keeps dying here" now also works when the bot had nothing to pick up, covers 32 blocks, and a bot that
  respawns inside such an area can walk out of it.
- Replacing a worn pickaxe no longer restarts every second and interrupts the main task.
- The night shelter waits 30 seconds before trying again on an unsafe spot.

## [0.8.2] - 2026-10-15

Fixes and additions from a long playtest. Tested in-game: an @gamer run with the new bed step, running when
outnumbered, the see-through path, the new HUD text and plan labels, the pickup fix and torch crafting.
Unit-tested only so far: junk pit / lava, furnace reuse, creeper fights, torch placing, avoiding death spots.

### Added
- **Bed early in @gamer**: right after stone tools the bot gets one bed (AltoClef only collected beds at the very
  end), so it can sleep through the first night. Setting, on by default.
- **Torches (optional, off by default)**: crafts torches from coal it carries (keeps about 16) and places one
  every 7 blocks or so where it's dark, so mobs don't spawn around it and you can see.
- **Runs when outnumbered**: with 3 or more hostile mobs within 8 blocks the bot runs away at once and keeps
  going until fewer than 2 are near, then returns to its task (it used to fight 5 zombies with a stone sword). If it can't get away within
  30 seconds it stops running for a minute and lets AltoClef fight or tower up.
  Number adjustable in Settings (0 = off).
- **Avoids places where it keeps dying**: after repeated deaths in one spot it walks around that area
  (10 blocks) for 10 minutes. Before, the message said "not going back there" but only skipped the items.
- **Fights creepers that keep following** (hit, step back, repeat) instead of running from them for minutes.
  If one starts hissing up close it still runs. Setting, on by default; safe-zone rules apply.
- **Junk goes where it can't be picked up again**: into lava the bot can see nearby, otherwise into a 1x1 hole
  3 blocks deep dug right beside it.

### Changed
- **Camera looks ahead while walking**: Baritone often walks staring at its feet; while the bot is only
  walking, your view now looks ahead (a little down at most). When it mines, places or fights you still see
  exactly where it aims. Visual only; setting in Camera.
- The path line is drawn through blocks, like Baritone's, so you can follow it behind walls; the next path
  segment is shown too.
- The HUD says "Hunting sheep" when the bot kills animals for food or wool, instead of "Fighting mobs".
- The @gamer plan names steps as goals ("Stock up food (4/20)") instead of finished facts ("Food stocked").

### Fixed
- The early bed no longer loops (AltoClef places the bed to set spawn; the bot then fetched it back).
- The bot placed a new furnace instead of walking a few blocks to the one it already had.
- AltoClef's quick hits on nearby mobs while mining were made with an empty hand; it now switches to a weapon.
- A pickup that has started is finished, even if the bot breaks blocks on the way to the item (it kept
  switching between picking up and mining).

## [0.8.1] - 2026-10-15

Fixes found while playing 0.8.0 (tested in-game).

### Fixed
- Fixed a crash ("TimeoutWanderTask ... null") when stuck recovery walked the bot away.
- Mining no longer restarts the same block over and over: drops are picked up after a block is broken, not
  in the middle of breaking the next one, and a block already in reach is always finished first.
- The new pickup step no longer interrupts block breaking, waits a moment between pickups and ignores items
  already being picked up.
- The HUD no longer shows [Recovering] for minutes while a side job (like picking up items) runs.
- "Defend myself when idle" is now the first switch in Settings > Basics.

## [0.8.0] - 2026-10-15

Building from .litematic schematics, smarter fighting, water safety, a world overlay and many fixes from
in-game testing. Tested in-game: building a schematic end to end (gather → build → done, pause survives a
restart), idle defense with critical hits, waiting while your inventory is open, the HUD progress line and
the overlay. The rest is unit-tested — please report anything odd.

### Added
- **Building (Build tab)**: build any `.litematic` schematic in survival — no Litematica mod needed. Pick a file
  (built-in or from `.minecraft/schematics`), it's placed where you stand facing your way (move it with
  X/Y/Z buttons, Rotate, or Relocate back to you) with an outline preview. The bot gathers the materials
  itself (one at a time, chests first), clears what's in the way and builds layer by layer from the bottom.
  Pause keeps a build (even after a restart) and Resume continues it; Stop asks twice because it forgets
  the build; builds queue up; finished builds have a Repair button. Blocks survival can't place (command
  blocks, spawners, barriers) or items it can't get make the build stop and wait for your approval.
- **Jump critical hits** in every fight: the bot waits for a full charge bar, jumps and hits on the way down
  (only where that's safe: not in water or lava, on ladders, in boats or under a low ceiling).
- **Axe or sword by situation**: an axe against shields and when one axe hit finishes the mob, a sword for
  groups (sweeping), otherwise whatever does the most damage per second. Netherite now counts too.
- **Avoid water in caves** (on by default): paths through cave water cost much more, so the bot goes around
  instead of wading and drowning. Lakes, rivers and oceans under open sky are not affected.
- **Getting air when drowning**: low on air underwater, the bot swims straight up if the surface is close;
  under a cave ceiling it places a door next to itself (doors can't hold water) and breathes inside it;
  with no door and no open water above, it leaves the way out to the pathfinder.
- **Carries a door** for those air pockets, made only from wood it already has.
- **Your own presets**: type a command and press *+* next to *Run* to keep it as a preset button;
  shift-click a preset to remove it.
- **Chest rules**: the bot only takes what it needs from chests if allowed (on), also from chests in safe
  zones (on, can be switched off), and always leaves at least a set amount behind (default one stack, 64).
- **Item frames are never touched**: the bot never hits an item frame or painting and never breaks the block
  one hangs on (frames often label chests).
- **Never attack players** (no PvP, on by default) in Safety rules. AltoClef's own `punk` / `terminate`
  commands still work when you run them yourself.
- **AltoClef options in the panel**: 18 of AltoClef's own settings (auto-eat, auto-respawn, replant crops,
  mining and chest search range, food to keep and collect, building blocks to keep, ...) can now be changed
  in Settings. They are applied in memory; AltoClef's own settings file is left untouched.
- The water bucket in the iron-kit gear gate has its own switch, and the cave-water cost and the visible
  inventory click pace can be adjusted in Settings.
- **Visible inventory** (on by default): when the bot crafts in its 2x2 grid, sorts or equips, the inventory
  screen opens so you can watch it click, at a slower human-like pace, and closes when it's done.
- **Drops outdated tools**: a wooden pickaxe goes once a stone one is carried, and so on (enchanted, named,
  needed or protected tools are kept). Separate setting from the junk drop.
- **Smelts in the background**: with 20+ seconds of smelting left and coal ore right nearby, the bot mines
  some coal instead of standing at the furnace (at most a minute, twice per batch), then walks back (setting,
  on by default).
- **Our own world overlay** (replaces Baritone's while on): blocks the bot will break glow red, the block it
  is mining orange, its goal green, the path ahead is a cyan line, and nearby hostile mobs get red outlines.
  Every layer can be switched off in Settings; switching the overlay off brings Baritone's back. Works with
  every graphics setting, including Fabulous (so do the safe-zone outlines now).
- **Your inventory is respected**: open it while the bot runs and the bot waits (no clicks, no walking,
  nothing closes it); close it and the bot carries on.

### Changed
- The HUD task line is now our own plain-English line: what you asked for and what the bot is doing right
  now, with progress, e.g. *Get iron pickaxe (0/1): Mining iron ore* or *Get spruce log x40 (12/40): Picking
  up items*. AltoClef's own
  top task name often stayed the same for minutes (a crafting table while it was really mining iron).
- The Food / Pearls / Rods / Eyes / Beds line only shows while *Beat the game* (`@gamer`) runs.
- Pressing the pause key with nothing running now says *Nothing to pause*.
- After an update, the HUD says so once and the Status log lists what's new.
- Panel: Settings use two columns on wide screens (far fewer pages) and long labels are cut neatly; the
  Status tab's hook list wraps instead of running off the screen; the Zones tab no longer draws the selected
  zone's name over the corner hint, and its rules sit in two columns.
- Full-height safe zones now also show their rectangle at your feet level, not only the corner posts.

### Fixed
- Underground, when it needs planks it mines nearby plank blocks (mineshafts) instead of walking off
  to find a tree.
- Fixed a crash when leaving a world while the bot was fighting ("other is null" in AltoClef's task chains).
- **Spare pickaxe**: the only pickaxe is replaced at 15% durability while the materials are still in the bag
  (same tier, else stone or wood). A pickaxe breaking deep underground used to send the bot back up for wood.
- **Picks up what it mines**: a new pickup step walks straight to nearby drops the task needs (within 10
  blocks, once they've landed), one at a time, and gives up on one after 10 seconds. AltoClef often said
  "collecting" but stood still or walked off to the next tree and left the drops behind.
- Steady mining now also covers dropped items: once the bot walks to a drop it picks it up before going back
  to mining, instead of flipping between the item and a block (seen 44 times in one test run).
- Side jobs (spare tools, equipping, junk drops, gear gates) no longer interrupt AltoClef while it is placing
  a crafting table, crafting or using a furnace. In a test run this made it re-place the same crafting table
  over and over.
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
