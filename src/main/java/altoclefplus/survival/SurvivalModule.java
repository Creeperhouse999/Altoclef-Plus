package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.Settings;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import altoclefplus.Plus;
import altoclefplus.core.PlusChain;
import altoclefplus.core.logic.Module;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.core.logic.events.StuckDetected;
import altoclefplus.core.logic.ItemInfo;
import altoclefplus.survival.logic.BoatTravel;
import altoclefplus.survival.logic.CautionPolicy;
import altoclefplus.core.TaskTargets;
import altoclefplus.survival.logic.GatePlanner;
import altoclefplus.survival.logic.GateScope;
import altoclefplus.survival.logic.IdleGuard;
import altoclefplus.survival.logic.WaterSafety;
import altoclefplus.survival.logic.StashPlanner;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalTwoBlocks;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Progression gates, caution, shield / dodging and the water-bucket clutch setting. */
public final class SurvivalModule implements Module {
    private static final int DECIDE_EVERY_TICKS = 20;

    private final AltoClef mod;
    private final PlusChain gateChain;
    private final PlusChain cautionChain;
    private final PlusChain boatChain;
    private final PlusChain stashChain;
    /** Above tasks, food and normal fighting, below fleeing (70+): drowning beats almost everything. */
    private final PlusChain airChain;
    /** Fighting a creeper that keeps following: above running from mobs (70), below a hissing creeper (50-100). */
    private final PlusChain creeperChain;
    /** Running when outnumbered: above AltoClef fighting (65) and its own run-away (70-80). */
    private final PlusChain fleeChain;
    private altoclefplus.survival.logic.Outnumbered outnumbered;
    /** Waiting out an early night: above the task and gear gates, below eating (55) and fighting. */
    private final PlusChain shelterChain;
    private final altoclefplus.survival.logic.CreeperChase creeperChase = new altoclefplus.survival.logic.CreeperChase(80);
    private final StashPlanner stashes = new StashPlanner();
    private String lastCommand = "";
    private final GatePlanner gates = new GatePlanner();
    private final CautionPolicy caution = new CautionPolicy();
    private boolean resting;
    private final CombatAssist combat = new CombatAssist();
    /** Hand AltoClef's mob defense the controls while idle and AFK; give them back ~5 s after it's calm. */
    private final IdleGuard idleGuard = new IdleGuard(100);
    /** Last tick the player pressed a movement/attack/use key (they are playing, not AFK). */
    private long lastInputTick = Long.MIN_VALUE / 2;
    private long nextAirTryMs;
    private GatePlanner.Gate runningGate;
    /** After any boat attempt, wait before considering another (stops retry loops at awkward shores). */
    private long nextBoatTryMs;
    private long ticks;
    private String status = "idle";

    public SurvivalModule(AltoClef mod) {
        this.mod = mod;
        gateChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus gear gates", 51f);
        cautionChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus caution", 53f, 2 * 60_000L);
        boatChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus boat", 52.5f, 4 * 60_000L);
        stashChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus stashes", 51.5f);
        airChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus air", 66f, 60_000L);
        creeperChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus creeper", 72f, 30_000L);
        shelterChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus night shelter", 54f, 10 * 60_000L);
        fleeChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus run away", 85f, 60_000L);
        Plus.bus().subscribe(StuckDetected.class, e -> {
            if (gateChain.isActive() && runningGate != null) failGate("stuck: " + e.kind());
        });
    }

    @Override
    public String id() {
        return "survival";
    }

    @Override
    public void tick(Snapshot s) {
        PlusConfig.Survival cfg = Plus.config().survival;
        guardWhileIdle(s, cfg);
        if (cfg.airRescue && (s.userTaskRunning || idleGuard.engaged())) rescueAir();
        if (cfg.fightChasingCreepers && (s.userTaskRunning || idleGuard.engaged())) fightChasingCreeper(s);
        if (cfg.runWhenOutnumbered > 0 && (s.userTaskRunning || idleGuard.engaged())) runIfOutnumbered(cfg);
        if (s.userTaskRunning || idleGuard.engaged()) combat.tick(mod, cfg);
        else combat.stop(mod);
        if (++ticks % DECIDE_EVERY_TICKS != 0) return;
        if (cfg.waterClutch) ensureAutoMlg();
        if (!s.userTaskRunning || s.mobDefenseActive || s.screenOpen || s.dead) return;

        long now = System.currentTimeMillis();
        // A gate job that ended without finishing (timed out or cancelled) counts as a failed attempt.
        if (runningGate != null && !gateChain.isActive()) failGate("did not finish");

        // A rest that ended (finished or timed out) below resume health didn't help: back off for a while.
        if (resting && !cautionChain.isActive()) {
            resting = false;
            if (s.health < cfg.resumeHealth) caution.restEndedWithoutHealing(s.seconds());
        }
        if (!cautionChain.isActive()) {
            boolean hasFood = ItemInfo.totalFoodUnits(s) > 0;
            CautionPolicy.Action a = caution.decide(s.health, s.hostilesNear16, s.y, hasFood, s.food, s.seconds(), cfg);
            if (a == CautionPolicy.Action.HOLD_AND_EAT) {
                resting = cautionChain.startJob(new RestTask(), "Low health: resting and eating", null);
            } else if (a == CautionPolicy.Action.FLEE) {
                cautionChain.startJob(new RunAwayFromHostilesTask(24, true), "Low health: getting away from mobs", null);
            }
        }
        if (cautionChain.isActive()) {
            status = cautionChain.jobLabel();
            return;
        }

        // Gear gates, boats and stashes wait while AltoClef is placing, crafting or using a container.
        if (altoclefplus.core.logic.TaskChainState.inDelicateStep(s.taskChain)) return;

        String cmd = Plus.control().lastCommand();
        if (!cmd.equals(lastCommand)) {
            stashes.reset();
            gates.resetRun();
            lastCommand = cmd;
        }
        if (cfg.nightShelter && cmd.contains("gamer")) shelterAtNight(s);
        if (shelterChain.isActive()) {
            status = shelterChain.jobLabel();
            return;
        }
        if (gateChain.isActive() || boatChain.isActive() || stashChain.isActive()) {
            status = gateChain.isActive() ? gateChain.jobLabel() : boatChain.isActive() ? boatChain.jobLabel() : stashChain.jobLabel();
            return;
        }
        if (cfg.boatTravel && tryBoat(s, cfg)) return;
        if (!"overworld".equals(s.dimension)) {
            status = "idle";
            return;
        }
        GateScope.Scope scope = GateScope.Scope.valueOf(cfg.gateScope);
        Optional<GatePlanner.Need> need = gates.next(s, cfg, now,
                GateScope.allowed(scope, s.userCommand, TaskTargets.of(mod)));
        if (need.isEmpty()) {
            status = "idle";
            tryStash(s, cfg);
            return;
        }
        GatePlanner.Need n = need.get();
        Task task = taskFor(n);
        if (task == null) {
            gates.markFailed(n.gate(), now);
            return;
        }
        String label = "Gear first: " + (n.item().equals("food") ? "food" : n.item().replace('_', ' '));
        if (gateChain.startJob(task, label, () -> runningGate = null)) {
            runningGate = n.gate();
            status = label;
        }
    }

    /**
     * AltoClef switches its whole task runner off when no task is running, so mob defense stopped too and
     * the bot just stood there while zombies and creepers killed it. Switch the runner on while a threat is
     * near (only when nobody is at the keys), so AltoClef's own defense fights or flees, then off again.
     */
    private void guardWhileIdle(Snapshot s, PlusConfig.Survival cfg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            // Left the world while defending: hand the runner back (it crashed AltoClef otherwise).
            if (idleGuard.tick(false, false, false, false) == IdleGuard.Action.RELEASE) {
                Plus.control().setDefending(false);
                if (!mod.getUserTaskChain().isActive()) mod.getTaskRunner().disable();
            }
            return;
        }
        if (s.dead || s.paused) return;
        if (anyKeyPressed(client.options)) lastInputTick = s.tick;
        boolean playerActive = s.tick - lastInputTick < 100;
        boolean threat = client.player.hurtTime > 0 || !client.world.getEntitiesByClass(HostileEntity.class,
                client.player.getBoundingBox().expand(10), e -> e.isAlive()
                        && (client.player.canSee(e) || e instanceof CreeperEntity c && c.getFuseSpeed() > 0)).isEmpty();
        switch (idleGuard.tick(cfg.defendWhileIdle, s.userTaskRunning, playerActive, threat)) {
            case ENGAGE -> {
                mod.getTaskRunner().enable();
                Plus.control().setDefending(true);
                Plus.log().add("survival", "Mob nearby while idle: defending");
                status = "Defending (idle)";
            }
            case RELEASE -> {
                Plus.control().setDefending(false);
                if (!mod.getUserTaskChain().isActive()) {
                    mod.getTaskRunner().disable();
                    mod.getClientBaritone().getPathingBehavior().cancelEverything();
                    mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
                }
                status = "idle";
            }
            case NONE -> { }
        }
    }

    /** Low on air underwater: swim up or make a door air pocket (AltoClef only jumps, and not while pathing). */
    private void rescueAir() {
        var p = mod.getPlayer();
        if (p == null || airChain.isActive() || !p.isSubmergedInWater()) return;
        if (System.currentTimeMillis() < nextAirTryMs) return;
        WaterSafety.AirAction a = WaterSafety.airAction(p.getAir(), true, waterAbove(p), SurfaceForAirTask.carriedDoor(mod) != null);
        if (a == WaterSafety.AirAction.NONE) return;
        String label = a == WaterSafety.AirAction.SWIM_UP ? "Low on air: swimming up" : "Low on air: door air pocket";
        if (airChain.startUrgent(new SurfaceForAirTask(a), label)) {
            Plus.log().add("survival", label);
            nextAirTryMs = System.currentTimeMillis() + 5_000;
        }
    }

    private void runIfOutnumbered(PlusConfig.Survival cfg) {
        var p = mod.getPlayer();
        if (p == null || p.world == null) return;
        if (outnumbered == null) outnumbered = new altoclefplus.survival.logic.Outnumbered(cfg.runWhenOutnumbered, 600, 1200);
        java.util.function.Predicate<HostileEntity> chasing = e -> e.isAlive()
                && !(e instanceof net.minecraft.entity.mob.EndermanEntity) && !(e instanceof net.minecraft.entity.mob.ZombifiedPiglinEntity);
        int close = p.world.getEntitiesByClass(HostileEntity.class, p.getBoundingBox().expand(8), chasing).size();
        int around = p.world.getEntitiesByClass(HostileEntity.class, p.getBoundingBox().expand(16), chasing).size();
        int pillagers = p.world.getEntitiesByClass(net.minecraft.entity.mob.PillagerEntity.class, p.getBoundingBox().expand(24),
                e -> e.isAlive()).size();
        var chest = p.getInventory().getArmorStack(2).getItem();
        boolean armored = chest == net.minecraft.item.Items.IRON_CHESTPLATE || chest == net.minecraft.item.Items.DIAMOND_CHESTPLATE
                || chest == net.minecraft.item.Items.NETHERITE_CHESTPLATE;
        close = altoclefplus.survival.logic.Outnumbered.effectiveClose(close, pillagers, armored, cfg.runWhenOutnumbered);
        around = altoclefplus.survival.logic.Outnumbered.effectiveClose(around, pillagers, armored, 2);
        boolean run = outnumbered.shouldRun(close, around, ticks);
        if (run && !fleeChain.isActive()) {
            if (fleeChain.startUrgent(new RunAwayFromHostilesTask(24, true), "Outnumbered (" + close + " mobs): running away")) {
                Plus.log().add("survival", "Outnumbered (" + close + " mobs): running away");
            }
        } else if (!run && fleeChain.isActive()) {
            fleeChain.cancelJob();
            Plus.log().add("survival", "Got away - back to the task");
        }
    }

    private void shelterAtNight(Snapshot s) {
        var p = mod.getPlayer();
        if (p == null || shelterChain.isActive()) return;
        var chest = p.getInventory().getArmorStack(2).getItem();
        int tier = chest == net.minecraft.item.Items.IRON_CHESTPLATE || chest == net.minecraft.item.Items.DIAMOND_CHESTPLATE
                || chest == net.minecraft.item.Items.NETHERITE_CHESTPLATE ? 4 : 0;
        boolean canSleep = mod.getItemStorage().hasItem(adris.altoclef.util.helpers.ItemHelper.BED)
                || mod.getBlockTracker().anyFound(adris.altoclef.util.helpers.ItemHelper.itemsToBlocks(adris.altoclef.util.helpers.ItemHelper.BED));
        int blocks = mod.getItemStorage().getItemCount(mod.getModSettings().getThrowawayItems(mod));
        String dim = p.world.getRegistryKey().getValue().getPath();
        if (!altoclefplus.survival.logic.NightShelter.shouldShelter(true, p.world.getTimeOfDay(), dim, tier, canSleep,
                p.isTouchingWater(), blocks)) return;
        if (shelterChain.startJob(new NightShelterTask(), "Night without armor: sheltering", null)) {
            Plus.log().add("survival", "Night without armor or bed: sheltering until dawn");
        }
    }

    private void fightChasingCreeper(Snapshot s) {
        var p = mod.getPlayer();
        if (p == null || creeperChain.isActive() || p.isTouchingWater()) return;
        var creepers = p.world.getEntitiesByClass(CreeperEntity.class, p.getBoundingBox().expand(10), e -> e.isAlive());
        CreeperEntity nearest = creepers.stream().min(java.util.Comparator.comparingDouble(p::distanceTo)).orElse(null);
        boolean weapon = !altoclefplus.survival.logic.CritTiming.bestWeapon(s.items).isEmpty();
        if (!creeperChase.shouldFight(nearest != null, nearest == null ? 99 : p.distanceTo(nearest), weapon, s.health, s.tick)) return;
        if (creeperChain.startUrgent(new CreeperFightTask(nearest), "A creeper keeps following: fighting it")) {
            Plus.log().add("survival", "A creeper keeps following: fighting it");
            creeperChase.reset();
        }
    }

    /** Water blocks above the head up to open air, or -1 if something solid is in the way. */
    private static int waterAbove(net.minecraft.entity.player.PlayerEntity p) {
        BlockPos pos = new BlockPos(p.getEyePos());
        for (int i = 0; i < 24; i++) {
            BlockPos b = pos.up(i);
            var st = p.world.getBlockState(b);
            if (st.isAir()) return i;
            if (st.getFluidState().isEmpty() && st.getMaterial().blocksMovement()) return -1;
        }
        return -1;
    }

    /**
     * The player is physically holding a movement/attack/use key. KeyBinding.isPressed can't be used: AltoClef
     * presses the same bindings itself (crit jumps, placing doors), which would look like the player playing.
     */
    private static boolean anyKeyPressed(GameOptions o) {
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        for (var kb : new net.minecraft.client.option.KeyBinding[]{o.forwardKey, o.backKey, o.leftKey, o.rightKey,
                o.jumpKey, o.attackKey, o.useKey, o.sneakKey}) {
            var key = ((altoclefplus.mixin.KeyBindingAccessor) kb).altoclefplus$getBoundKey();
            if (key == null || key.getCode() < 0) continue;
            boolean down = key.getCategory() == net.minecraft.client.util.InputUtil.Type.MOUSE
                    ? org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, key.getCode()) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                    : key.getCategory() == net.minecraft.client.util.InputUtil.Type.KEYSYM
                    && net.minecraft.client.util.InputUtil.isKeyPressed(window, key.getCode());
            if (down) return true;
        }
        return false;
    }

    /** Bed by the portal, valuables chest, backup gear - each once per run, all optional. */
    private void tryStash(Snapshot s, PlusConfig.Survival cfg) {
        Optional<StashPlanner.Job> job = stashes.next(s, cfg, portalNearby());
        if (job.isEmpty()) return;
        StashPlanner.Job j = job.get();
        stashes.markDone(j); // once, even if it fails - never loops
        Task task = switch (j) {
            case BED_AT_PORTAL -> new PlaceBedAndSetSpawnTask();
            case VALUABLES -> new StoreInAnyContainerTask(false, StashPlanner.VALUABLES.stream()
                    .filter(s::has)
                    .map(id -> new ItemTarget(Registry.ITEM.get(new Identifier("minecraft", id)), s.count(id)))
                    .toArray(ItemTarget[]::new));
            case BACKUP_BEFORE_NETHER, BACKUP_BEFORE_END -> new BackupGearTask();
        };
        String label = switch (j) {
            case BED_AT_PORTAL -> "Setting spawn with a bed by the portal";
            case VALUABLES -> "Leaving valuables in a chest";
            case BACKUP_BEFORE_NETHER -> "Leaving backup gear before the Nether";
            case BACKUP_BEFORE_END -> "Leaving backup gear before the End";
        };
        stashChain.startJob(task, label, null);
    }

    private boolean portalNearby() {
        BlockPos c = mod.getPlayer().getBlockPos();
        var w = mod.getWorld();
        for (BlockPos b : BlockPos.iterate(c.add(-8, -4, -8), c.add(8, 4, 8))) {
            if (w.getBlockState(b).isOf(Blocks.NETHER_PORTAL)) return true;
        }
        return false;
    }

    /** Big water straight ahead on the way to a far goal, and a boat in the bag: cross by boat. */
    private boolean tryBoat(Snapshot s, PlusConfig.Survival cfg) {
        if (!s.pathing || !"overworld".equals(s.dimension) || ItemInfo.countMatching(s, id -> id.endsWith("_boat")) == 0) return false;
        BlockPos goal = goalPos();
        if (goal == null) return false;
        double dist = Math.hypot(goal.getX() - s.x, goal.getZ() - s.z);
        if (dist < cfg.boatMinGoalDistance) return false;
        List<Boolean> surface = new ArrayList<>();
        double dx = (goal.getX() - s.x) / dist;
        double dz = (goal.getZ() - s.z) / dist;
        var world = mod.getWorld();
        for (int i = 1; i <= 64; i++) {
            int x = (int) Math.floor(s.x + dx * i);
            int z = (int) Math.floor(s.z + dz * i);
            boolean water = false;
            for (int y = (int) s.y + 1; y >= (int) s.y - 3 && !water; y--) {
                BlockPos b = new BlockPos(x, y, z);
                water = world.getFluidState(b).isOf(Fluids.WATER) && world.isAir(b.up());
            }
            surface.add(water);
        }
        if (!BoatTravel.shouldBoat(surface, dist, cfg.boatMinWaterRun, 3, cfg.boatMinGoalDistance)) return false;
        if (System.currentTimeMillis() < nextBoatTryMs) return false;
        nextBoatTryMs = System.currentTimeMillis() + 45_000;
        return boatChain.startJob(new BoatCrossTask(goal), "Crossing water by boat", null);
    }

    private BlockPos goalPos() {
        Goal g = mod.getClientBaritone().getPathingBehavior().getGoal();
        if (g instanceof GoalBlock b) return b.getGoalPos();
        if (g instanceof GoalGetToBlock b) return b.getGoalPos();
        if (g instanceof GoalTwoBlocks b) return b.getGoalPos();
        if (g instanceof GoalNear b) return b.getGoalPos();
        if (g instanceof GoalXZ b) return new BlockPos(b.getX(), 64, b.getZ());
        return null;
    }

    private static Task taskFor(GatePlanner.Need n) {
        if (n.item().equals("food")) return new CollectFoodTask(n.count());
        if (n.item().equals("bed")) return TaskCatalogue.getItemTask("bed", 1);
        Item item = Registry.ITEM.get(new Identifier("minecraft", n.item()));
        if (item == Items.AIR) return null;
        return TaskCatalogue.getItemTask(item, n.count());
    }

    private void failGate(String why) {
        if (runningGate != null) {
            gates.markFailed(runningGate, System.currentTimeMillis());
            Plus.log().add("survival", "Gear gate " + runningGate + " gave up for now (" + why + ")");
        }
        runningGate = null;
        gateChain.cancelJob();
    }

    private static Field autoMlgField;

    /** Make sure AltoClef's water-bucket clutch is on (in memory only; AltoClef's file is untouched). */
    private void ensureAutoMlg() {
        Settings settings = mod.getModSettings();
        if (settings == null || settings.shouldAutoMLGBucket()) return;
        try {
            if (autoMlgField == null) {
                autoMlgField = Settings.class.getDeclaredField("autoMLGBucket");
                autoMlgField.setAccessible(true);
            }
            autoMlgField.setBoolean(settings, true);
            Plus.log().add("survival", "Turned on AltoClef's water bucket clutch");
        } catch (ReflectiveOperationException e) {
            Plus.log().add("survival", "Could not turn on water bucket clutch: " + e.getMessage());
        }
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public void onDisable() {
        if (idleGuard.engaged() && !mod.getUserTaskChain().isActive()) mod.getTaskRunner().disable();
        gateChain.cancelJob();
        airChain.cancelJob();
        creeperChain.cancelJob();
        fleeChain.cancelJob();
        stashChain.cancelJob();
        boatChain.cancelJob();
        cautionChain.cancelJob();
        combat.stop(mod);
    }
}
