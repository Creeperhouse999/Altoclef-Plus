# Changelog

All notable changes to AltoClef Plus are listed here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/). Each release on the
[Releases page](https://github.com/Creeperhouse999/Altoclef-Plus/releases) uses the matching section below
as its notes.

## [Unreleased]

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
