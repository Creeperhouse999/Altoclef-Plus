package altoclefplus.ui;

import altoclefplus.Plus;
import altoclefplus.core.logic.PlusConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Rows shown in the Settings tab. Changes apply immediately; "Save" writes the config file. */
public final class SettingsModel {
    public sealed interface Row permits Header, Toggle, Value, Action, Choice {}

    /** Cycles through options; labels are shown, values are stored. */
    public record Choice(String label, List<String> values, List<String> labels, Supplier<String> get, Consumer<String> set) implements Row {
        public String currentLabel() {
            int i = values.indexOf(get.get());
            return i < 0 ? get.get() : labels.get(i);
        }

        public void next() {
            int i = values.indexOf(get.get());
            set.accept(values.get((i + 1) % values.size()));
        }
    }

    public record Header(String label) implements Row {}

    public record Toggle(String label, BooleanSupplier get, Consumer<Boolean> set) implements Row {}

    public record Value(String label, IntSupplier get, IntConsumer set, int min, int max, int step) implements Row {}

    public record Action(String label, Runnable run) implements Row {}

    private SettingsModel() {}

    public static List<Row> rows(Runnable openProtected, Runnable openJunk, Runnable openHotbar,
                                 Runnable openNeverAttack, Runnable openNeverBreak) {
        PlusConfig c = Plus.config();
        List<Row> r = new ArrayList<>();

        r.add(new Header("Basics"));
        r.add(new Toggle("Defend myself when idle (AFK)", () -> c.survival.defendWhileIdle, v -> c.survival.defendWhileIdle = v));
        r.add(new Toggle("Loot chests", () -> c.basics.lootChests, v -> c.basics.lootChests = v));
        r.add(new Toggle("Sleep at night (@gamer)", () -> c.basics.sleepAtNight, v -> c.basics.sleepAtNight = v));
        r.add(new Toggle("Loot ruined portals (@gamer)", () -> c.basics.lootRuinedPortals, v -> c.basics.lootRuinedPortals = v));
        r.add(new Toggle("Loot desert temples (@gamer)", () -> c.basics.lootDesertTemples, v -> c.basics.lootDesertTemples = v));
        r.add(new Toggle("Pearls by piglin trading, not hunting", () -> c.basics.barterForPearls, v -> c.basics.barterForPearls = v));
        r.add(new Value("Eyes of ender to collect", () -> c.basics.eyesToCollect, v -> c.basics.eyesToCollect = v, 12, 20, 1));
        r.add(new Toggle("Take needed items from chests", () -> c.basics.takeFromChests, v -> c.basics.takeFromChests = v));
        r.add(new Toggle("  ...also chests in safe zones", () -> c.basics.takeFromZoneChests, v -> c.basics.takeFromZoneChests = v));
        r.add(new Value("  Leave at least this many in a chest", () -> c.basics.chestLeaveAtLeast, v -> c.basics.chestLeaveAtLeast = v, 0, 256, 16));

        r.add(new Header("Safety rules"));
        r.add(new Toggle("Never attack named mobs", () -> c.rules.neverAttackNamed, v -> c.rules.neverAttackNamed = v));
        r.add(new Toggle("Never attack tamed pets", () -> c.rules.neverAttackTamed, v -> c.rules.neverAttackTamed = v));
        r.add(new Toggle("Never attack players (no PvP)", () -> c.rules.neverAttackPlayers, v -> c.rules.neverAttackPlayers = v));
        r.add(new Toggle("Self-defense inside zones", () -> c.rules.selfDefense, v -> c.rules.selfDefense = v));
        r.add(new Action("Never-attack mob list...", openNeverAttack));
        r.add(new Action("Never-break block list...", openNeverBreak));
        r.add(new Toggle("Show safe zones in the world", () -> c.ui.showZones, v -> c.ui.showZones = v));
        r.add(new Toggle("World overlay (instead of Baritone's)", () -> c.ui.overlay, v -> c.ui.overlay = v));
        r.add(new Toggle("  Overlay: path line", () -> c.ui.overlayPath, v -> c.ui.overlayPath = v));
        r.add(new Toggle("  Overlay: blocks to break", () -> c.ui.overlayBreak, v -> c.ui.overlayBreak = v));
        r.add(new Toggle("  Overlay: block being mined", () -> c.ui.overlayTarget, v -> c.ui.overlayTarget = v));
        r.add(new Toggle("  Overlay: goal", () -> c.ui.overlayGoal, v -> c.ui.overlayGoal = v));
        r.add(new Toggle("  Overlay: nearby hostile mobs", () -> c.ui.overlayMobs, v -> c.ui.overlayMobs = v));

        r.add(new Header("Modules"));
        module(r, "Control panel & HUD", "ui", v -> c.modules.ui = v, () -> c.modules.ui);
        module(r, "Smooth camera (experimental)", "camera", v -> c.modules.camera = v, () -> c.modules.camera);
        module(r, "Smarter inventory", "inventory", v -> c.modules.inventory = v, () -> c.modules.inventory);
        module(r, "Survival & progression", "survival", v -> c.modules.survival = v, () -> c.modules.survival);
        module(r, "Stuck recovery", "recovery", v -> c.modules.recovery = v, () -> c.modules.recovery);

        r.add(new Header("HUD & chat"));
        r.add(new Toggle("Show HUD", () -> c.ui.hud, v -> c.ui.hud = v));
        r.add(new Toggle("Hide AltoClef's overlay text", () -> c.ui.hideAltoClefOverlay, v -> c.ui.hideAltoClefOverlay = v));
        r.add(new Toggle("Bot messages to log, not chat", () -> c.ui.redirectChat, v -> c.ui.redirectChat = v));
        r.add(new Toggle("Wait while my inventory is open", () -> c.ui.pauseWhileInventoryOpen, v -> c.ui.pauseWhileInventoryOpen = v));
        r.add(new Toggle("Block @ and # chat commands", () -> c.ui.blockChatCommands, v -> c.ui.blockChatCommands = v));

        r.add(new Header("Camera"));
        r.add(new Value("Smoothness (higher = snappier)", () -> Math.round(c.camera.smoothness), v -> c.camera.smoothness = v, 1, 40, 1));
        r.add(new Toggle("Look ahead while walking (not at feet)", () -> c.camera.lookAhead, v -> c.camera.lookAhead = v));
        r.add(new Value("Combat speed-up (x)", () -> Math.round(c.camera.combatMultiplier), v -> c.camera.combatMultiplier = v, 1, 10, 1));

        r.add(new Header("Inventory"));
        r.add(new Toggle("Fixed hotbar layout", () -> c.inventory.fixHotbar, v -> c.inventory.fixHotbar = v));
        r.add(new Toggle("Use the right tool", () -> c.inventory.fixTools, v -> c.inventory.fixTools = v));
        r.add(new Toggle("Wear better armor", () -> c.inventory.equipArmor, v -> c.inventory.equipArmor = v));
        r.add(new Toggle("Craft armor piece by piece", () -> c.inventory.craftArmorPiecewise, v -> c.inventory.craftArmorPiecewise = v));
        r.add(new Toggle("Keep spare tools", () -> c.inventory.spareTools, v -> c.inventory.spareTools = v));
        r.add(new Toggle("Drop junk when full", () -> c.inventory.dropJunk, v -> c.inventory.dropJunk = v));
        r.add(new Toggle("Drop outdated tools", () -> c.inventory.dropOutdatedTools, v -> c.inventory.dropOutdatedTools = v));
        r.add(new Toggle("Pick up dropped items right away", () -> c.inventory.pickupDrops, v -> c.inventory.pickupDrops = v));
        r.add(new Toggle("Visible inventory (watch the bot sort and craft)", () -> c.inventory.visibleInventory, v -> c.inventory.visibleInventory = v));
        r.add(new Value("  Click pace (ms)", () -> Math.round(c.inventory.visibleClickDelay * 1000), v -> c.inventory.visibleClickDelay = v / 1000f, 100, 1000, 50));
        r.add(new Toggle("Smelt in the background (get coal meanwhile)", () -> c.inventory.backgroundSmelting, v -> c.inventory.backgroundSmelting = v));
        r.add(new Toggle("Unstick cursor items", () -> c.inventory.cursorGuard, v -> c.inventory.cursorGuard = v));
        r.add(new Toggle("New table/furnace instead of long walks", () -> c.inventory.placeNearCraftingTable, v -> c.inventory.placeNearCraftingTable = v));
        r.add(new Value("Drop junk when free slots <=", () -> c.inventory.junkFreeSlotThreshold, v -> c.inventory.junkFreeSlotThreshold = v, 0, 10, 1));
        r.add(new Value("Max crafting table distance", () -> c.inventory.craftingTableMaxDistance, v -> c.inventory.craftingTableMaxDistance = v, 8, 256, 8));
        r.add(new Action("Edit protected items...", openProtected));
        r.add(new Action("Edit junk items...", openJunk));
        r.add(new Action("Edit hotbar layout...", openHotbar));

        r.add(new Header("Progression gates"));
        r.add(new Choice("Gear up for", List.of("NEEDED", "GAMER_ONLY", "ALL"),
                List.of("Tasks that need it", "@gamer only", "Every task"),
                () -> c.survival.gateScope, v -> c.survival.gateScope = v));
        r.add(new Toggle("Stone sword/axe/shovel first", () -> c.survival.gateStoneKit, v -> c.survival.gateStoneKit = v));
        r.add(new Toggle("Iron sword and shield", () -> c.survival.gateIronKit, v -> c.survival.gateIronKit = v));
        r.add(new Toggle("Bed early in @gamer (after stone tools)", () -> c.survival.gateEarlyBed, v -> c.survival.gateEarlyBed = v));
        r.add(new Toggle("  ...and a water bucket", () -> c.survival.gateWaterBucket, v -> c.survival.gateWaterBucket = v));
        r.add(new Toggle("Full iron armor before diamonds", () -> c.survival.gateIronArmor, v -> c.survival.gateIronArmor = v));
        r.add(new Toggle("Armor + food before Nether", () -> c.survival.gateNetherEntry, v -> c.survival.gateNetherEntry = v));

        r.add(new Header("Survival"));
        r.add(new Toggle("Caution (heal, flee, avoid caves)", () -> c.survival.caution, v -> c.survival.caution = v));
        r.add(new Toggle("Avoid water in caves", () -> c.survival.avoidCaveWater, v -> c.survival.avoidCaveWater = v));
        r.add(new Value("  Cave water cost (per block)", () -> (int) Math.round(c.survival.caveWaterPenalty), v -> c.survival.caveWaterPenalty = v, 0, 200, 10));
        r.add(new Toggle("Get air when drowning (swim up / door)", () -> c.survival.airRescue, v -> c.survival.airRescue = v));
        r.add(new Toggle("Carry a door for air pockets", () -> c.survival.carryDoor, v -> c.survival.carryDoor = v));
        r.add(new Toggle("Jump critical hits", () -> c.survival.critHits, v -> c.survival.critHits = v));
        r.add(new Toggle("Pick axe or sword by situation", () -> c.survival.smartWeapon, v -> c.survival.smartWeapon = v));
        r.add(new Toggle("Steady mining (no target switching)", () -> c.survival.steadyMining, v -> c.survival.steadyMining = v));
        r.add(new Toggle("Fair combat (no hits through walls)", () -> c.survival.fairCombat, v -> c.survival.fairCombat = v));
        r.add(new Toggle("Door fixes (no getting stuck at doors)", () -> c.survival.fixDoors, v -> c.survival.fixDoors = v));
        r.add(new Toggle("Use shield", () -> c.survival.shield, v -> c.survival.shield = v));
        r.add(new Toggle("Better projectile dodging", () -> c.survival.dodge, v -> c.survival.dodge = v));
        r.add(new Toggle("Carry water for fall clutch", () -> c.survival.waterClutch, v -> c.survival.waterClutch = v));
        r.add(new Toggle("Recover items after death", () -> c.survival.deathRecovery, v -> c.survival.deathRecovery = v));
        r.add(new Toggle("Precise Nether travel to stronghold", () -> c.survival.preciseNetherTravel, v -> c.survival.preciseNetherTravel = v));
        r.add(new Value("Use the Nether for trips over (blocks)", () -> c.survival.netherTravelMinDistance, v -> c.survival.netherTravelMinDistance = v, 200, 3000, 100));
        r.add(new Toggle("Hunt endermen with boat trap", () -> c.survival.endermanBoatHunt, v -> c.survival.endermanBoatHunt = v));
        r.add(new Toggle("Boats across oceans", () -> c.survival.boatTravel, v -> c.survival.boatTravel = v));
        r.add(new Value("Boat: min water length", () -> c.survival.boatMinWaterRun, v -> c.survival.boatMinWaterRun = v, 8, 96, 4));

        r.add(new Header("Safety stashes"));
        r.add(new Toggle("Leave a bed by the Nether portal", () -> c.survival.bedNearPortal, v -> c.survival.bedNearPortal = v));
        r.add(new Toggle("Chest valuables before Nether", () -> c.survival.stashValuablesBeforeNether, v -> c.survival.stashValuablesBeforeNether = v));
        r.add(new Toggle("Backup gear chest before Nether", () -> c.survival.backupGearBeforeNether, v -> c.survival.backupGearBeforeNether = v));
        r.add(new Toggle("Backup gear chest before End", () -> c.survival.backupGearBeforeEnd, v -> c.survival.backupGearBeforeEnd = v));

        r.add(new Header("Stuck recovery"));
        r.add(new Toggle("Show recovery actions", () -> c.recovery.logActions, v -> c.recovery.logActions = v));
        r.add(new Value("Stuck after (seconds)", () -> c.recovery.noProgressSeconds, v -> c.recovery.noProgressSeconds = v, 10, 300, 5));
        r.add(new Value("Safe mode after (rounds)", () -> c.recovery.safeModeAfterCycles, v -> c.recovery.safeModeAfterCycles = v, 1, 10, 1));
        r.add(new Value("Stop bot after safe modes (0=never)", () -> c.recovery.stopAfterSafeModeCycles, v -> c.recovery.stopAfterSafeModeCycles = v, 0, 10, 1));

        // AltoClef's own options (kept as overrides in our config, applied in memory).
        Object altoSettings = altoclefplus.Plus.mod() == null ? null : altoclefplus.Plus.mod().getModSettings();
        if (altoSettings != null) {
            r.add(new Header("AltoClef options"));
            for (altoclefplus.core.logic.AltoClefOptions.Option o : altoclefplus.core.logic.AltoClefOptions.ALL) {
                if (Double.isNaN(altoclefplus.core.logic.AltoClefOptions.read(altoSettings, o.field()))) continue;
                java.util.function.DoubleSupplier current = () -> c.altoclefOverrides.getOrDefault(o.field(),
                        altoclefplus.core.logic.AltoClefOptions.read(altoSettings, o.field()));
                java.util.function.DoubleConsumer change = v -> {
                    c.altoclefOverrides.put(o.field(), v);
                    altoclefplus.core.logic.AltoClefOptions.applyAll(altoSettings, c.altoclefOverrides);
                };
                if (o.kind() == altoclefplus.core.logic.AltoClefOptions.Kind.TOGGLE) {
                    r.add(new Toggle(o.label(), () -> current.getAsDouble() >= 0.5, v -> change.accept(v ? 1 : 0)));
                } else {
                    r.add(new Value(o.label(), () -> (int) Math.round(current.getAsDouble()), v -> change.accept(v), o.min(), o.max(), o.step()));
                }
            }
        }
        return r;
    }

    private static void module(List<Row> r, String label, String id, Consumer<Boolean> setCfg, BooleanSupplier getCfg) {
        r.add(new Toggle(label, getCfg, v -> {
            setCfg.accept(v);
            if (Plus.modules() != null) Plus.modules().setEnabled(id, v);
        }));
    }
}
