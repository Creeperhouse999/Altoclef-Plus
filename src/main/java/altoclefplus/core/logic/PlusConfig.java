package altoclefplus.core.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** All user-tunable settings. Field initializers are the defaults; Gson keeps them for missing keys. */
public final class PlusConfig {
    public Modules modules = new Modules();
    public Ui ui = new Ui();
    public Camera camera = new Camera();
    public Inventory inventory = new Inventory();
    public Survival survival = new Survival();
    public Recovery recovery = new Recovery();

    public static final class Modules {
        public boolean ui = true;
        public boolean camera = true;
        public boolean inventory = true;
        public boolean survival = true;
        public boolean recovery = true;
    }

    public static final class Preset {
        public String name;
        /** One or more AltoClef commands (without prefix) separated by ';'. */
        public String command;

        public Preset() {}

        public Preset(String name, String command) {
            this.name = name;
            this.command = command;
        }
    }

    public static final class Ui {
        public boolean hud = true;
        public boolean hudTask = true;
        public boolean hudBadge = true;
        public boolean hudPlan = true;
        public boolean hudRecovery = true;
        public boolean hudResources = true;
        public boolean hideAltoClefOverlay = true;
        public boolean redirectChat = true;
        public boolean blockChatCommands = true;
        /** Item ids, or the tokens "food" and "bed" which sum all foods / beds. */
        public List<String> hudResourceItems = new ArrayList<>(List.of("food", "ender_pearl", "blaze_rod", "ender_eye", "bed"));
        public List<Preset> presets = new ArrayList<>(List.of(
                new Preset("Iron gear", "get iron_pickaxe 1;get iron_sword 1;get iron_chestplate 1;get iron_leggings 1;get iron_helmet 1;get iron_boots 1"),
                new Preset("Diamond gear", "get diamond_pickaxe 1;get diamond_sword 1;get diamond_chestplate 1;get diamond_leggings 1;get diamond_helmet 1;get diamond_boots 1"),
                new Preset("Food (20)", "food 20"),
                new Preset("Bed", "get white_bed 1"),
                new Preset("Iron tools", "get iron_pickaxe 1;get iron_axe 1;get iron_shovel 1")));
    }

    public static final class Camera {
        /** Exponential smoothing rate per second. Higher = snappier. */
        public float smoothness = 10f;
        public float maxDegreesPerSecond = 720f;
        public float combatMultiplier = 3f;
        /** Player moved further than this in one tick (teleport) = snap camera. */
        public float snapDistance = 8f;
    }

    public static final class Inventory {
        public boolean fixHotbar = true;
        public boolean fixTools = true;
        public boolean equipArmor = true;
        public boolean craftArmorPiecewise = true;
        public boolean spareTools = true;
        public boolean dropJunk = true;
        public boolean cursorGuard = true;
        public boolean placeNearCraftingTable = true;
        public int junkFreeSlotThreshold = 3;
        public int buildingBlockCapPerType = 64;
        public int buildingBlockCapTotal = 128;
        public int craftingTableMaxDistance = 32;
        public int furnaceMaxDistance = 32;
        public int hotbarHysteresisSeconds = 10;
        public List<String> protectedItems = new ArrayList<>(List.of(
                "diamond", "iron_ingot", "gold_ingot", "ender_pearl", "ender_eye", "blaze_rod", "blaze_powder",
                "obsidian", "flint_and_steel", "bucket", "water_bucket", "lava_bucket", "shield",
                "crafting_table", "furnace", "golden_apple", "enchanted_golden_apple"));
        /** Only these items (plus building blocks above their cap) may ever be dropped as junk. */
        public List<String> junkItems = new ArrayList<>(List.of(
                "gunpowder", "spider_eye", "rotten_flesh", "poisonous_potato", "bone", "feather", "egg",
                "ink_sac", "wheat_seeds", "beetroot_seeds", "pumpkin_seeds", "melon_seeds",
                "oak_sapling", "birch_sapling", "spruce_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling"));
        /** Building blocks kept up to the per-type cap; extra stacks may be dropped. */
        public List<String> buildingBlocks = new ArrayList<>(List.of(
                "cobblestone", "dirt", "gravel", "netherrack", "diorite", "andesite", "granite", "tuff",
                "cobbled_deepslate", "blackstone", "basalt", "end_stone", "sand", "red_sand", "soul_sand", "soul_soil"));
        public boolean dropUnknownItems = false;
        /**
         * Category -> hotbar slot index (0-8). Pickaxe in slot 0 and blocks in slot 8 match where Baritone puts
         * them itself, so the two never fight over a slot.
         */
        public Map<String, Integer> hotbarLayout = new LinkedHashMap<>(Map.of(
                "pickaxe", 0, "sword", 1, "axe", 2, "shovel", 3, "food", 4, "water_bucket", 5, "blocks", 8));
    }

    public static final class Survival {
        /** NEEDED (@gamer + tasks whose items need gear), GAMER_ONLY or ALL. */
        public String gateScope = "NEEDED";
        public boolean gateStoneKit = true;
        public boolean gateIronKit = true;
        public boolean gateIronArmor = true;
        public boolean gateNetherEntry = true;
        public int netherMinFoodUnits = 20;
        public int gateFailuresBeforeSkip = 3;
        public int gateSkipMinutes = 10;

        public boolean caution = true;
        public float holdHealth = 12f;
        public float resumeHealth = 18f;
        public float fleeHealth = 8f;
        public float noDescendHealth = 14f;
        public int noDescendY = 40;

        /** Only hit mobs the bot can see and is facing (AltoClef hits through walls / backwards). */
        public boolean fairCombat = true;
        public int fairCombatMaxAngle = 30;

        /** Path around doors with diagonal/step moves and don't eat while aiming at doors. */
        public boolean fixDoors = true;

        public boolean shield = true;
        public boolean dodge = true;
        public boolean waterClutch = true;

        public boolean deathRecovery = true;
        public int deathKitDistance = 150;
        public int deathKitBelowY = 50;
        public int deathPickupRadius = 8;

        public boolean bedNearPortal = true;
        public boolean stashValuablesBeforeNether = false;
        public boolean backupGearBeforeNether = false;
        public boolean backupGearBeforeEnd = false;

        /** Validated stronghold estimates, decide-once Nether travel, obsidian before entering. */
        public boolean preciseNetherTravel = true;
        public int netherTravelMinDistance = 800;

        public boolean endermanBoatHunt = true;
        public boolean boatTravel = true;
        public int boatMinWaterRun = 24;
        public int boatMinGoalDistance = 48;
    }

    public static final class Recovery {
        public boolean logActions = true;
        public int noProgressSeconds = 30;
        public double noProgressDistance = 2.0;
        public int pillarWindowSeconds = 60;
        public int pillarDeltaY = 24;
        public double pillarMaxHorizontal = 6.0;
        public int netherCeilingY = 120;
        public int sameBlockWindowSeconds = 60;
        public int sameBlockStarts = 8;
        public int sameBlockMaxSeconds = 20;
        public int flipFlopWindowSeconds = 45;
        public double flipFlopShare = 0.8;
        public int flipFlopMaxCells = 3;
        public int flipFlopMinRepaths = 4;
        public int restartWindowSeconds = 120;
        public int restartCount = 4;
        public int baseCooldownSeconds = 10;
        public int maxCooldownSeconds = 120;
        public int resetAfterCleanSeconds = 120;
        /** Full ladder cycles without progress before safe mode. */
        public int safeModeAfterCycles = 3;
        public int safeModeRetryMinutes = 5;
        /** 0 = never stop the bot; otherwise stop after this many safe-mode rounds. */
        public int stopAfterSafeModeCycles = 0;
        public int deathLoopDeaths = 2;
        public int deathLoopMinutes = 10;
        public int deathLoopRadius = 32;
        /** Overworld -> Nether -> Overworld round trips within the window that count as ping-pong. */
        public int pingPongRoundTrips = 2;
        public int pingPongMinutes = 10;
        /** Leaf task name fragments during which the watchdog stays quiet. */
        public List<String> longWaitTasks = new ArrayList<>(List.of(
                "Smelt", "Furnace", "WaitForDragon", "Sleep", "Eat", "Idle"));
    }

    /** Replaces null sections/lists (from hand-edited JSON) with defaults. */
    public PlusConfig normalize() {
        PlusConfig d = new PlusConfig();
        if (modules == null) modules = d.modules;
        if (ui == null) ui = d.ui;
        if (camera == null) camera = d.camera;
        if (inventory == null) inventory = d.inventory;
        if (survival == null) survival = d.survival;
        if (recovery == null) recovery = d.recovery;
        if (ui.hudResourceItems == null) ui.hudResourceItems = d.ui.hudResourceItems;
        if (ui.presets == null) ui.presets = d.ui.presets;
        if (inventory.protectedItems == null) inventory.protectedItems = d.inventory.protectedItems;
        if (inventory.junkItems == null) inventory.junkItems = d.inventory.junkItems;
        if (inventory.buildingBlocks == null) inventory.buildingBlocks = d.inventory.buildingBlocks;
        if (inventory.hotbarLayout == null) inventory.hotbarLayout = d.inventory.hotbarLayout;
        if (recovery.longWaitTasks == null) recovery.longWaitTasks = d.recovery.longWaitTasks;

        // Hand-edited files: drop null entries and broken presets, keep numbers in sane ranges.
        ui.hudResourceItems = noNulls(ui.hudResourceItems);
        ui.presets = new ArrayList<>(ui.presets.stream()
                .filter(p -> p != null && p.name != null && !p.name.isBlank() && p.command != null && !p.command.isBlank())
                .toList());
        inventory.protectedItems = noNulls(inventory.protectedItems);
        inventory.junkItems = noNulls(inventory.junkItems);
        inventory.buildingBlocks = noNulls(inventory.buildingBlocks);
        inventory.hotbarLayout.entrySet().removeIf(e -> e.getKey() == null || e.getValue() == null);
        recovery.longWaitTasks = noNulls(recovery.longWaitTasks);

        camera.smoothness = clamp(camera.smoothness, 1, 40);
        camera.maxDegreesPerSecond = clamp(camera.maxDegreesPerSecond, 30, 5000);
        camera.combatMultiplier = clamp(camera.combatMultiplier, 1, 10);
        inventory.junkFreeSlotThreshold = clamp(inventory.junkFreeSlotThreshold, 0, 10);
        inventory.buildingBlockCapPerType = clamp(inventory.buildingBlockCapPerType, 16, 2304);
        inventory.buildingBlockCapTotal = clamp(inventory.buildingBlockCapTotal, inventory.buildingBlockCapPerType, 2304);
        inventory.craftingTableMaxDistance = clamp(inventory.craftingTableMaxDistance, 8, 512);
        recovery.noProgressSeconds = clamp(recovery.noProgressSeconds, 10, 600);
        recovery.safeModeAfterCycles = clamp(recovery.safeModeAfterCycles, 1, 20);
        recovery.stopAfterSafeModeCycles = clamp(recovery.stopAfterSafeModeCycles, 0, 20);
        try {
            altoclefplus.survival.logic.GateScope.Scope.valueOf(survival.gateScope);
        } catch (RuntimeException e) {
            survival.gateScope = d.survival.gateScope;
        }
        survival.boatMinWaterRun = clamp(survival.boatMinWaterRun, 8, 256);
        survival.netherTravelMinDistance = clamp(survival.netherTravelMinDistance, 100, 10_000);
        return this;
    }

    private static List<String> noNulls(List<String> list) {
        return new ArrayList<>(list.stream().filter(v -> v != null && !v.isBlank()).toList());
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clamp(float v, float min, float max) {
        return Float.isNaN(v) ? min : Math.max(min, Math.min(max, v));
    }
}
