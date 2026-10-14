package altoclefplus.ui;

import altoclefplus.Plus;
import altoclefplus.core.logic.PlusConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Rows shown in the Settings tab. Changes apply immediately; "Save" writes the config file. */
public final class SettingsModel {
    public sealed interface Row permits Header, Toggle, Value, Action {}

    public record Header(String label) implements Row {}

    public record Toggle(String label, BooleanSupplier get, Consumer<Boolean> set) implements Row {}

    public record Value(String label, IntSupplier get, IntConsumer set, int min, int max, int step) implements Row {}

    public record Action(String label, Runnable run) implements Row {}

    private SettingsModel() {}

    public static List<Row> rows(Runnable openProtected, Runnable openJunk, Runnable openHotbar) {
        PlusConfig c = Plus.config();
        List<Row> r = new ArrayList<>();

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
        r.add(new Toggle("Block @ and # chat commands", () -> c.ui.blockChatCommands, v -> c.ui.blockChatCommands = v));

        r.add(new Header("Camera"));
        r.add(new Value("Smoothness (higher = snappier)", () -> Math.round(c.camera.smoothness), v -> c.camera.smoothness = v, 1, 40, 1));
        r.add(new Value("Combat speed-up (x)", () -> Math.round(c.camera.combatMultiplier), v -> c.camera.combatMultiplier = v, 1, 10, 1));

        r.add(new Header("Inventory"));
        r.add(new Toggle("Fixed hotbar layout", () -> c.inventory.fixHotbar, v -> c.inventory.fixHotbar = v));
        r.add(new Toggle("Use the right tool", () -> c.inventory.fixTools, v -> c.inventory.fixTools = v));
        r.add(new Toggle("Wear better armor", () -> c.inventory.equipArmor, v -> c.inventory.equipArmor = v));
        r.add(new Toggle("Craft armor piece by piece", () -> c.inventory.craftArmorPiecewise, v -> c.inventory.craftArmorPiecewise = v));
        r.add(new Toggle("Keep spare tools", () -> c.inventory.spareTools, v -> c.inventory.spareTools = v));
        r.add(new Toggle("Drop junk when full", () -> c.inventory.dropJunk, v -> c.inventory.dropJunk = v));
        r.add(new Toggle("Unstick cursor items", () -> c.inventory.cursorGuard, v -> c.inventory.cursorGuard = v));
        r.add(new Toggle("New table/furnace instead of long walks", () -> c.inventory.placeNearCraftingTable, v -> c.inventory.placeNearCraftingTable = v));
        r.add(new Value("Drop junk when free slots <=", () -> c.inventory.junkFreeSlotThreshold, v -> c.inventory.junkFreeSlotThreshold = v, 0, 10, 1));
        r.add(new Value("Max crafting table distance", () -> c.inventory.craftingTableMaxDistance, v -> c.inventory.craftingTableMaxDistance = v, 8, 256, 8));
        r.add(new Action("Edit protected items...", openProtected));
        r.add(new Action("Edit junk items...", openJunk));
        r.add(new Action("Edit hotbar layout...", openHotbar));

        r.add(new Header("Progression gates"));
        r.add(new Toggle("Stone sword/axe/shovel first", () -> c.survival.gateStoneKit, v -> c.survival.gateStoneKit = v));
        r.add(new Toggle("Iron sword, shield, water bucket", () -> c.survival.gateIronKit, v -> c.survival.gateIronKit = v));
        r.add(new Toggle("Full iron armor before diamonds", () -> c.survival.gateIronArmor, v -> c.survival.gateIronArmor = v));
        r.add(new Toggle("Armor + food before Nether", () -> c.survival.gateNetherEntry, v -> c.survival.gateNetherEntry = v));

        r.add(new Header("Survival"));
        r.add(new Toggle("Caution (heal, flee, avoid caves)", () -> c.survival.caution, v -> c.survival.caution = v));
        r.add(new Toggle("Fair combat (no hits through walls)", () -> c.survival.fairCombat, v -> c.survival.fairCombat = v));
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
        return r;
    }

    private static void module(List<Row> r, String label, String id, Consumer<Boolean> setCfg, BooleanSupplier getCfg) {
        r.add(new Toggle(label, getCfg, v -> {
            setCfg.accept(v);
            if (Plus.modules() != null) Plus.modules().setEnabled(id, v);
        }));
    }
}
