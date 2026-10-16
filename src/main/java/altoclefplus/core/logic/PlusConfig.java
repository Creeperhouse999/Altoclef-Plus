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
    public Rules rules = new Rules();
    public Basics basics = new Basics();
    /** Safe zones of every world (each zone remembers its world). */
    public List<altoclefplus.zones.logic.Zone> zones = new ArrayList<>();
    /** AltoClef's own options changed from our panel: field name -> value (toggles as 0/1). */
    public java.util.Map<String, Double> altoclefOverrides = new java.util.HashMap<>();
    /** Build jobs (queued, paused, waiting, done), per world. */
    public List<altoclefplus.build.logic.BuildJob> builds = new ArrayList<>();

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
        /** While you have your inventory open the bot waits, and nothing closes it. */
        public boolean pauseWhileInventoryOpen = true;
        /** The bot waits while the AltoClef Plus panel is open (and AltoClef doesn't close it). */
        public boolean pauseWhilePanelOpen = true;
        /** Draw safe-zone outlines in the world. */
        public boolean showZones = false;
        /** Version the player last played with (for the one-time "updated" note). */
        public String lastSeenVersion = "";
        /** Our world overlay (replaces Baritone's): path, blocks to break, mining target, goal, mobs. */
        public boolean overlay = true;
        public boolean overlayPath = true;
        /** Build preview / running build: missing blocks as see-through blue cubes, wrong blocks outlined red. */
        public boolean overlayBlueprint = true;
        public boolean overlayBreak = true;
        public boolean overlayTarget = true;
        public boolean overlayGoal = true;
        public boolean overlayMobs = true;
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
        /** While the bot only walks, the view looks ahead instead of at its feet (visual only). */
        public boolean lookAhead = true;
    }

    public static final class Inventory {
        public boolean fixHotbar = true;
        public boolean fixTools = true;
        public boolean equipArmor = true;
        public boolean craftArmorPiecewise = true;
        public boolean spareTools = true;
        public boolean dropJunk = true;
        /** Drop a tool once a better one of the same kind is carried (wooden pickaxe after a stone one). */
        public boolean dropOutdatedTools = true;
        /** Walk to nearby drops the task needs right away (AltoClef sometimes left them behind). */
        public boolean pickupDrops = true;
        /** Craft torches from carried coal and place them in dark places (off by default). */
        /** Pick up crafting tables / furnaces the bot placed after using them, instead of crafting new ones. */
        public boolean carryStations = true;
        public boolean placeTorches = false;
        public boolean cursorGuard = true;
        /** Show the inventory screen while the bot moves items in its own inventory, at a watchable pace. */
        public boolean visibleInventory = true;
        /** Don't stand at the furnace: mine some coal while it smelts, then come back. */
        public boolean backgroundSmelting = true;
        /** Seconds between item moves in visible mode (AltoClef's default is 0.2). */
        public float visibleClickDelay = 0.3f;
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
        /** Part of the iron kit: a water bucket for fall clutches and putting out fire. */
        public boolean gateWaterBucket = true;
        /** @gamer: get one bed right after stone tools (AltoClef only gets beds at the very end). */
        public boolean gateEarlyBed = true;
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
        /** With no task running and nobody at the keys, fight or flee mobs instead of standing there. */
        public boolean defendWhileIdle = true;
        /** Wait for a full charge bar and hit on the way down from a jump (critical hits) when it's safe. */
        public boolean critHits = true;
        /** Fight a creeper that keeps following instead of running from it forever. */
        public boolean fightChasingCreepers = true;
        /** In @gamer, without iron armor or a bed, wait out the night in a covered 3-deep hole. */
        public boolean nightShelter = true;
        /** In deep water, swim (sprint under the surface) like a player instead of bobbing with jump. */
        public boolean swimLikePlayer = true;
        /** Run away when this many hostile mobs are within 8 blocks (0 = off), until fewer than 2 are near. */
        public int runWhenOutnumbered = 3;
        /** Make paths through water in caves expensive (open lakes and oceans are not affected). */
        public boolean avoidCaveWater = true;
        public double caveWaterPenalty = 40;
        /** Low on air underwater: swim up, or make an air pocket with a door. */
        public boolean airRescue = true;
        /** Keep a wooden door in the bag for underwater air pockets. */
        public boolean carryDoor = true;
        /** Axe vs sword by situation (shields, groups, finishing blows) instead of AltoClef's fixed list. */
        public boolean smartWeapon = true;
        /** Stay on the block being mined; only switch to a much closer one, at most every few seconds. */
        public boolean steadyMining = true;
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

    /** What the bot may break and attack outside (and inside) safe zones. */
    public static final class Rules {
        public boolean neverAttackNamed = true;
        public boolean neverAttackTamed = true;
        /** No PvP: never hit other players (friends on LAN). */
        public boolean neverAttackPlayers = true;
        /** Inside zones, still hit a mob that is attacking the bot (never named/tamed ones). */
        public boolean selfDefense = true;
        /** Entity type ids never attacked anywhere, e.g. "villager". */
        public List<String> neverAttackMobs = new ArrayList<>();
        /** Block ids never broken anywhere, e.g. "chest". */
        public List<String> neverBreakBlocks = new ArrayList<>();
    }

    /** Everyday options, mapped onto AltoClef's own settings while the game runs. */
    public static final class Basics {
        public boolean lootChests = true;
        public boolean sleepAtNight = true;
        public boolean lootRuinedPortals = true;
        public boolean lootDesertTemples = true;
        public boolean barterForPearls = false;
        public int eyesToCollect = 14;
        /** The bot may take items it needs from chests (gathering and building). */
        public boolean takeFromChests = true;
        /** ...also from chests inside safe zones (otherwise zones with "no looting" are left alone). */
        public boolean takeFromZoneChests = true;
        /** Always leave at least this many of an item in a chest (64 = one stack). */
        public int chestLeaveAtLeast = 64;
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
        if (rules == null) rules = d.rules;
        if (basics == null) basics = d.basics;
        if (zones == null) zones = new ArrayList<>();
        if (altoclefOverrides == null) altoclefOverrides = new java.util.HashMap<>();
        if (builds == null) builds = new ArrayList<>();
        builds.removeIf(j -> j == null || j.file == null || j.world == null || j.state == null);
        for (var j : builds) {
            if (j.skipBlocks == null) j.skipBlocks = new ArrayList<>();
            if (j.pendingSkip == null) j.pendingSkip = new ArrayList<>();
        }
        altoclefOverrides.values().removeIf(v -> v == null || !Double.isFinite(v));
        zones.removeIf(z -> z == null || z.world == null);
        for (altoclefplus.zones.logic.Zone z : zones) {
            if (z.rules == null) z.rules = new altoclefplus.zones.logic.ZoneRules();
            if (z.name == null || z.name.isBlank()) z.name = "Zone";
        }
        if (rules.neverAttackMobs == null) rules.neverAttackMobs = new ArrayList<>();
        if (rules.neverBreakBlocks == null) rules.neverBreakBlocks = new ArrayList<>();
        rules.neverAttackMobs = noNulls(rules.neverAttackMobs);
        rules.neverBreakBlocks = noNulls(rules.neverBreakBlocks);
        basics.eyesToCollect = clamp(basics.eyesToCollect, 12, 20);
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
        survival.caveWaterPenalty = Double.isFinite(survival.caveWaterPenalty) ? Math.max(0, Math.min(200, survival.caveWaterPenalty)) : 40;
        if (!Float.isFinite(inventory.visibleClickDelay)) inventory.visibleClickDelay = 0.3f;
        inventory.visibleClickDelay = clamp(inventory.visibleClickDelay, 0.1f, 1.0f);
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
