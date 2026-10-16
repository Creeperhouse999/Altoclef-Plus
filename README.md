<div align="center">

# AltoClef Plus

**A smarter, safer, easier-to-use AltoClef.**
Control panel instead of chat commands, a cleaner HUD, smoother camera, better inventory handling,
safer progression and automatic recovery when the bot gets stuck.

[![Minecraft 1.18.2](https://img.shields.io/badge/Minecraft-1.18.2-62B47A?style=flat-square)](https://minecraft.wiki/w/Java_Edition_1.18.2)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=flat-square)](https://fabricmc.net/use/installer/)
[![Requires AltoClef 0.4.1](https://img.shields.io/badge/Requires-AltoClef%200.4.1-5865F2?style=flat-square)](https://github.com/gaucho-matrero/altoclef/releases/tag/v0.4.1)
[![Requires Baritone](https://img.shields.io/badge/Requires-Baritone%20%28in%20AltoClef%200.4.1%29-3B3B3B?style=flat-square)](https://github.com/cabaletta/baritone)
[![Latest release](https://img.shields.io/github/v/release/Creeperhouse999/Altoclef-Plus?include_prereleases&style=flat-square&label=Release)](https://github.com/Creeperhouse999/Altoclef-Plus/releases)
[![Modrinth](https://img.shields.io/badge/Modrinth-AltoClef%20Plus-1BD96A?style=flat-square&logo=modrinth&logoColor=white)](https://modrinth.com/mod/altoclef-plus)
[![Build](https://img.shields.io/github/actions/workflow/status/Creeperhouse999/Altoclef-Plus/build.yml?branch=main&style=flat-square)](https://github.com/Creeperhouse999/Altoclef-Plus/actions/workflows/build.yml)

[Download](https://github.com/Creeperhouse999/Altoclef-Plus/releases) ·
[Install](#install) ·
[How to use](#how-to-use) ·
[Roadmap](#roadmap) ·
[Build from source](#build-from-source) ·
[Report a bug](https://github.com/Creeperhouse999/Altoclef-Plus/issues/new/choose)

</div>

> [!IMPORTANT]
> **AltoClef Plus is an unofficial add-on.** It is not made by, endorsed by or affiliated with the
> [AltoClef](https://github.com/gaucho-matrero/altoclef) or [Baritone](https://github.com/cabaletta/baritone)
> teams. Please don't report AltoClef Plus problems to them — use this repo's
> [issues](https://github.com/Creeperhouse999/Altoclef-Plus/issues) instead.

---

## What is it?

[AltoClef](https://github.com/gaucho-matrero/altoclef) is a bot that plays Minecraft by itself — it can
even beat the game with `@gamer`. It moves around using [Baritone](https://github.com/cabaletta/baritone).

AltoClef Plus is a **separate mod you add next to them**. It doesn't replace or modify their files. Its
goal is to fix the things that make watching the bot frustrating:

| Today, without AltoClef Plus | The goal with AltoClef Plus |
|---|---|
| You type `@` commands in chat | You click buttons in a control panel |
| Chat floods with bot messages | Messages go to a log you can open when you want |
| The camera snaps and shakes | The camera turns smoothly (the bot still aims instantly) |
| Mines logs holding a helmet, swaps hotbar items nonstop | Uses the right tool and keeps a tidy hotbar |
| Runs for diamonds with no armor and no sword | Gears up first: stone sword, then iron kit and armor |
| Gets stuck pillaring or mining the same block forever | Notices, tries something else, never gives up |
| Dies and starts over | Goes back for its items before they despawn |
| Bounces between the Nether and the Overworld on the way to the stronghold | Gathers obsidian first, then uses the Nether once to come out right at the stronghold |

> [!NOTE]
> AltoClef Plus is **in early development**. All features below are built and unit-tested, but they
> haven't had wide in-game testing yet — please [report anything odd](https://github.com/Creeperhouse999/Altoclef-Plus/issues/new/choose).
> Every feature can be switched off in *Settings*.

## Features

Legend: ✅ built · 🧪 new, only unit-tested so far (please report anything odd) · 🚧 being built now · 🗓️ planned (not available yet)

### Control panel and HUD ✅
- **Control panel** (Right Shift) with tabs: *Tasks*, *Presets*, *Plan*, *Status*, *Build*, *Zones*, *Settings*. ✅
- **Presets tab:** about 45 ready-made presets (tools, armor, kits, resources, food, speedrun) and a kit
  builder: pick items and amounts ("a stack of dirt, 16 bread, iron sword, ...") and save it as your own
  preset; the bot gathers it all in one go. 🧪
- **Blueprint blocks:** the build preview shows the schematic's missing blocks as see-through blue cubes and
  wrong blocks outlined in red, like Litematica. 🧪
- **Start anything with a click:** Beat the game (`@gamer`), get any item from a searchable item grid,
  presets like "Iron gear" or "Food", or any other AltoClef command. ✅
- **Stop, Pause and Resume** buttons and hotkeys. ✅
- **Plan view:** a checklist of what the bot is doing in `@gamer` — ✓ done, ▶ now, ○ next. ✅
- **Step history:** every step the bot worked on and how long it took. ✅
- **Small HUD:** what you asked for, how far along it is and what the bot is doing right now in plain
  English (e.g. *Get spruce log x40 (12/40): Mining spruce log*), state (Running / Paused / Recovering / Defending / Idle), next milestones,
  alerts, and during `@gamer` the key items (food, pearls, blaze rods, eyes, beds). ✅
- **World overlay:** blocks the bot will break glow red, the block it is mining orange, its goal green, the
  path ahead is a cyan line and nearby hostile mobs are outlined — each layer can be switched off. 🧪
- **Quiet chat:** AltoClef and Baritone messages go to the panel log; `@` / `#` chat commands are turned
  off (you can turn them back on). ✅
- AltoClef's own overlay text is hidden. ✅

### Smooth camera ✅ *(experimental)*
- Your view turns smoothly instead of snapping.
- Only what *you see* is smoothed — the bot still aims, mines and fights instantly.
- One click to turn off if you prefer the raw view.

### Smarter inventory ✅
- Uses the right tool: axe for wood, shovel for dirt and sand, pickaxe for stone.
- Fixed hotbar layout (pickaxe, sword, axe, shovel, food, water bucket … blocks last) — no more slot swapping.
- Wears better armor as soon as it has it.
- Crafts armor **piece by piece** as materials come in, instead of waiting for a full set.
- Keeps spare tools and replaces them before they break.
- Throws away junk (like random gunpowder) when the inventory fills up. Only items on a junk list (and
  extra stacks of cobblestone, dirt and similar) are ever dropped — never tools, armor, enchanted or
  renamed items, valuables, or anything the current task needs. You can edit both lists.
- Drops tools once it has a better one of the same kind (a wooden pickaxe after a stone one). 🧪
- **Visible inventory:** when it crafts or sorts in its own inventory, the inventory screen opens so you can
  watch the clicks at a human-like pace. 🧪
- **Respects your inventory:** open it while the bot runs and the bot waits until you close it. 🧪
- **Smelts in the background:** fetches some coal instead of standing at the furnace, then comes back. 🧪
- Presets check at the end that everything is still there (a death can cost the chestplate) and redo
  what's missing, then put the armor on. 🧪
- Fixes items getting stuck on the cursor and the eye-of-ender crafting bug.
- Places a new crafting table instead of walking hundreds of blocks back to an old one.

### Safer progression and survival ✅
- **Fair combat:** no more hitting mobs through walls or while looking the other way. ✅
- **Defends itself when idle:** with no task running and you away from the keys, it fights or flees mobs
  instead of standing still; touch a key and you have the controls back. ✅
- **Steady mining:** sticks to the block it is going for instead of switching targets all the time. ✅
- **Better melee:** waits for a full charge bar and lands jump critical hits; picks an axe against shields or
  for a finishing blow and a sword for groups. 🧪
- **Water safety:** avoids wading through water in caves, swims up when low on air, or makes an air pocket
  with a door it carries when a ceiling is in the way. 🧪
- **Gear before risk:** stone sword before mining iron; iron pickaxe, sword, shield and water bucket next;
  full iron armor before hunting diamonds; armor and food before entering the Nether. ✅ Only for tasks that
  need it by default (`@gamer`, ores, Nether and End items) — errands like "get dirt" start right away.
- **No more getting stuck at doors.** ✅
- **Caution:** stops to eat and heal when low, flees when in danger, avoids going deep when weak. ✅
- **Shield:** puts one in the offhand and blocks arrows, fireballs and creepers. ✅
- **Better projectile dodging** (only onto safe ground). ✅
- **Water bucket clutch:** always carries water so falls don't kill it. ✅
- **Death recovery:** goes back to its death spot and picks its items up before they despawn. ✅
- **Enderman hunting with a boat trap** in a warped forest, killed with jump critical hits. ✅
- **Boats across oceans** and large lakes (not small ponds or narrow rivers). ✅
- **Safety stashes (optional):** leave a bed by the Nether portal, store valuables in a chest before the
  Nether, keep a backup gear set in a chest before the Nether or the End. ✅
- **Precise Nether travel to the stronghold:** brings obsidian and flint and steel, travels to the exact
  matching Nether coordinates, and builds an exit portal that drops it right next to the stronghold —
  one trip, no back-and-forth. ✅

### Safe zones and safety rules ✅
- **Safe zones:** mark your house, farm or petting zoo from the *Zones* tab (stand on two corners, press a
  button). Per zone you choose: no breaking, no placing, no looting, no entering, no attacking hostile /
  neutral / friendly mobs. Baritone plans around zones, and you can show their outlines in the world.
- **Never attack** named mobs, tamed pets and other players (all on by default), plus any mob types you pick.
- **Never break** the block types you pick.
- **Self-defense:** inside a zone the bot may still hit a mob that is attacking it (never named or tamed).

### AltoClef's own options 🧪
- 18 of AltoClef's settings right in the panel: eat and respawn automatically, replant crops, how far to
  mine or search chests for resources, how much food to keep, building blocks to keep, and more. They are
  applied in memory — AltoClef's own settings file is never changed.

### Stuck recovery ✅
- Detects: no progress, endless pillaring (the "Nether elevator"), mining the same block, walking back and
  forth, restarting the same task over and over.
- Fixes it step by step: new path → avoid that spot → restart the task → walk away and try again.
- If nothing works after a few rounds, it goes into **safe mode**: gets to safe ground, eats, tells you,
  and retries every few minutes instead of risking your run.
- Avoids areas where it keeps dying instead of walking back into the same trap.
- Catches dimension ping-pong (Overworld → Nether → Overworld with no progress) and impossible stronghold
  estimates, and picks a sane route instead.
- Doesn't stop the bot unless you want it to (there's a setting for that); every action shows in the HUD
  and log.

## Requirements

| | Version |
|---|---|
| Minecraft | **1.18.2** (Java Edition) |
| Mod loader | [Fabric Loader](https://fabricmc.net/use/installer/) 0.14 or newer |
| [AltoClef](https://github.com/gaucho-matrero/altoclef) | **0.4.1** — [download the official release](https://github.com/gaucho-matrero/altoclef/releases/tag/v0.4.1) |
| [Baritone](https://github.com/cabaletta/baritone) | Included in the AltoClef 0.4.1 release (see below) |
| Fabric API | **Not needed** |
| [Mod Menu](https://modrinth.com/mod/modmenu/versions?g=1.18.2) | Optional — adds AltoClef Plus to the mod list with a config button |

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft **1.18.2**
   (or create a Fabric 1.18.2 profile in your launcher, e.g. Modrinth App, Prism or MultiMC).
2. Download the official [AltoClef v0.4.1 release](https://github.com/gaucho-matrero/altoclef/releases/tag/v0.4.1)
   and put **both jars** from the zip into your `mods` folder.
3. Download the latest `altoclefplus-x.y.z.jar` from [Modrinth](https://modrinth.com/mod/altoclef-plus)
   or [GitHub Releases](https://github.com/Creeperhouse999/Altoclef-Plus/releases) and put it in the same
   `mods` folder.
4. Start the game and join a world. Press **Right Shift** to open the control panel.

> [!TIP]
> **About Baritone:** the AltoClef 0.4.1 release zip contains two jars. `altoclef-0.4.0.jar` also carries
> a copy of Baritone inside it, and `baritone-unoptimized-fabric-1.18.2.jar` is the same Baritone as a
> separate file. Installing both (as the AltoClef release intends) is the recommended setup and what
> AltoClef Plus is tested with — Fabric notices it's the same mod and loads Baritone only once.

Your `mods` folder should look like this:

```
mods/
├── altoclef-0.4.0.jar
├── baritone-unoptimized-fabric-1.18.2.jar
└── altoclefplus-x.y.z.jar
```

## How to use

### Keys

| Key | What it does |
|---|---|
| **Right Shift** | Open / close the control panel |
| **K** | Stop the bot |
| **O** | Pause / resume the bot |
| Ctrl + K | AltoClef's own emergency stop (still works) |

All keys can be changed in *Options → Controls → Key Binds → AltoClef Plus*.

With [Mod Menu](https://modrinth.com/mod/modmenu/versions?g=1.18.2) installed you can also open the
settings from *Mods → AltoClef Plus → ⚙*.

### Control panel

| Tab | What's there |
|---|---|
| **Tasks** | Beat the game, get items (searchable icon grid + amount), presets, any AltoClef command (press *+* to keep a command as a preset, shift-click a preset to remove it) |
| **Presets** | Ready-made presets and your own; *Make a kit* to build an item list and save it (shift-click your preset to delete it, ctrl-click to edit it) |
| **Plan** | The `@gamer` checklist and the step history |
| **Status** | What the bot is doing right now, the log, inventory summary, module health |
| **Build** | Pick a .litematic, place and preview it (blueprint blocks), build, pause, resume |
| **Zones** | Your safe zones: create, rename, set rules, delete |
| **Settings** | Basics (chest looting, sleeping…), safety rules, every feature's on/off switch and tuning, and AltoClef's own options |

**Pause vs Stop:** AltoClef can't freeze a task halfway, so *Pause* stops the bot but remembers what it
was doing, and *Resume* starts the same command again. Nothing in your inventory is lost. *Stop* forgets
the command.

### Trying everything out

[docs/TESTING.md](docs/TESTING.md) is a short checklist that walks through every feature in a test world.

### Settings file

Everything in the Settings tab is saved to `.minecraft/altoclef/altoclefplus.json`. You can edit it by
hand; if the file is broken, AltoClef Plus uses defaults and tells you in the log instead of crashing.

## Roadmap

| Version | Focus | Status |
|---|---|---|
| **0.1** | Control panel, HUD, quiet chat, `@gamer` plan view, smooth camera, fair combat | ✅ Released |
| **0.2** | Smarter inventory: tools, hotbar, armor, junk, crafting fixes | ✅ Released |
| **0.3** | Safer progression: gear gates, caution, shield, dodging, water clutch | ✅ Released |
| **0.4** | Stuck recovery and death item recovery | ✅ Released |
| **0.5** | Enderman boat-trap hunting, boat travel, safety stashes, precise Nether travel to the stronghold | ✅ Released |
| **0.6** | Safe zones, safety rules, basics settings, door fix, gate scope | ✅ Released |
| **0.7** | In-game testing feedback, polish and fixes | ✅ Released |
| **0.8** | Building from .litematic files, better melee, water safety, world overlay, visible inventory | ✅ Released |
| **0.9** | Presets tab and kits, blueprint blocks, golden boots only for the Nether, many `@gamer` fixes | ✅ Released |
| **0.10** | Schematic contents (chests, signs, frames), chest labels, scaffolding cleanup, more fixes | 🗓️ Planned |
| **1.0** | Multiplayer parties (team `@gamer`, guard, build together) and lots of polish: the first stable release | 🗓️ Planned |

<sub><sup>psst… a website is on the way</sup></sub>

Live progress: the [milestones](https://github.com/Creeperhouse999/Altoclef-Plus/milestones) list every planned item as an issue, closed when it's done.
See [CHANGELOG.md](CHANGELOG.md) for what changed in each version.

## Build from source

You need **JDK 17** (Minecraft 1.18.2 mods are built with Java 17).

```bash
git clone https://github.com/Creeperhouse999/Altoclef-Plus.git
cd Altoclef-Plus
./gradlew build          # Windows: gradlew.bat build
```

- The first build downloads Minecraft, the Fabric toolchain and the official AltoClef v0.4.1 release
  (checksum-verified, unpacked into `libs/`). This takes a few minutes once.
- The mod jar ends up in `build/libs/altoclefplus-<version>.jar`.
- If your default Java isn't 17, point Gradle at a JDK 17:
  set `JAVA_HOME` (e.g. `export JAVA_HOME=/path/to/jdk-17`) or add
  `org.gradle.java.home=/path/to/jdk-17` to `~/.gradle/gradle.properties`.

Useful commands:

| Command | What it does |
|---|---|
| `./gradlew build` | Compile, run tests, build the jar |
| `./gradlew test` | Run the unit tests only |
| `bash tools/audit-mixins.sh` | Check that every mixin target exists in Minecraft / AltoClef / Baritone |
| `bash tools/test-summary.sh` | Print a one-line summary of the last test run |

## FAQ

**Is this an official AltoClef update?**
No. It's an independent add-on that runs next to AltoClef. The AltoClef and Baritone teams have nothing
to do with it.

**Does it work on servers?**
It's built and tested for singleplayer and LAN worlds. Use bots only where the server rules allow them.

**Do I still need AltoClef?**
Yes. AltoClef Plus adds to AltoClef; it can't run without it.

**Can I turn features off?**
Yes, every feature has its own switch in the Settings tab.

**Why can't I type `@gamer` in chat anymore?**
The control panel replaces chat commands. If you prefer chat, turn off
*Settings → Block chat commands*.

## Credits

- [AltoClef](https://github.com/gaucho-matrero/altoclef) by TacoTechnica (gaucho-matrero) and contributors — the bot itself.
- [Baritone](https://github.com/cabaletta/baritone) by leijurv, Brady and contributors — pathfinding.
- [Fabric](https://fabricmc.net/) — the mod loader.

## License

Copyright © 2026 Creeperhouse999. This is **source-available, not open source**: the
[LICENSE](LICENSE) explicitly allows you to read the code, build it and use it for yourself, and use the
official releases. Redistributing it or publishing modified versions is not allowed without permission.
