# In-game test checklist

A quick way to check every feature in a real game. Use a **new singleplayer test world** (Survival,
normal difficulty) and keep `.minecraft/logs/latest.log` if anything goes wrong — attach it to a
[bug report](https://github.com/Creeperhouse999/Altoclef-Plus/issues/new/choose).

Tip: open the panel's **Status** tab — the *Hooks* line turns green for each part of AltoClef that
AltoClef Plus has successfully plugged into. A hook that stays grey after you've used that feature means
it didn't attach (please report it).

## 1. Loading
- [ ] Game starts with `altoclef`, `baritone` and `altoclefplus` in the mods folder (no crash).
- [ ] With Mod Menu installed: *Mods → AltoClef Plus* shows the icon, description and links; the ⚙ button
      opens the Settings tab.
- [ ] *Options → Controls → Key Binds* has an **AltoClef Plus** section (Right Shift / K / P).

## 2. Panel, HUD and chat
- [ ] Right Shift opens the panel; it stays open while the bot moves.
- [ ] Tasks tab: search "diamond", click the diamond pickaxe, amount 1, **Get it** → bot starts.
- [ ] **P** pauses (HUD shows *Paused*), **P** again resumes the same task, **K** stops.
- [ ] HUD (top left) shows state, current task, and (during *Beat the game*) the next milestones.
- [ ] Typing `@gamer` in chat does **not** start the bot and shows a one-line hint.
- [ ] AltoClef's own white task text on screen is gone; bot messages appear in Status → Log, not chat.
- [ ] Settings: switch a module off and on; **Save**; restart the game → setting kept.

## 3. Camera *(experimental)*
- [ ] While the bot runs, the view turns smoothly; the bot still mines and fights normally.
- [ ] When you take control (bot stopped) the camera responds instantly to your mouse.

## 4. Fair combat
- [ ] Bot doesn't hit mobs behind walls; it turns to face a mob when it hits it.

## 5. Inventory
- [ ] Bot chops trees with an axe and digs dirt with a shovel (never with a helmet or crafting table).
- [ ] Hotbar items no longer flicker between hand and inventory.
- [ ] Give yourself iron armor pieces (`/give @s iron_chestplate`): the bot puts them on.
- [ ] Give 8 iron ingots after the bot has iron pickaxe/sword/shield/bucket: it crafts a chestplate.
- [ ] Fill the inventory with junk (`/give @s gunpowder 64`, cobblestone...): it drops gunpowder and extra
      cobblestone, but keeps diamonds, tools and armor.
- [ ] `get ender_eye 2` with blaze powder and pearls (`/give`): crafts eyes without looping.

## 6. Survival
- [ ] After a stone pickaxe it makes a stone sword, axe and shovel; after an iron pickaxe: iron sword,
      shield, water bucket, then iron armor.
- [ ] With low health and no mobs around, it stops to eat and heal.
- [ ] Skeleton shooting at it: shield goes up (shield in offhand).
- [ ] Fall from a height: water bucket clutch.

## 7. Stuck recovery and death
- [ ] Trap the bot (e.g. surround it while it walks to a target): within ~30 s the HUD shows a recovery
      step (new path → skip spot → restart → walk away).
- [ ] Die from a mob/fall while the bot runs: after respawn it walks back and picks the items up.

## 8. Travel and pearls (later in a *Beat the game* run)
- [ ] Before the Nether it brings a boat; a bed is placed by the portal and spawn is set.
- [ ] In the Nether, pearls are hunted in a warped forest with the boat trap (panel: *Hunt endermen*).
- [ ] Stronghold: it throws two eyes from ~100 blocks apart and heads to a sensible spot (no y=1620),
      without bouncing between the Nether and the Overworld.
- [ ] On a long trip across an ocean it uses the boat.
