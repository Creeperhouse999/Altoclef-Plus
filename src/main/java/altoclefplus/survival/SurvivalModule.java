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
import altoclefplus.survival.logic.GatePlanner;
import altoclefplus.survival.logic.StashPlanner;
import net.minecraft.block.Blocks;
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
    private final StashPlanner stashes = new StashPlanner();
    private String lastCommand = "";
    private final GatePlanner gates = new GatePlanner();
    private final CautionPolicy caution = new CautionPolicy();
    private final CombatAssist combat = new CombatAssist();
    private GatePlanner.Gate runningGate;
    /** After any boat attempt, wait before considering another (stops retry loops at awkward shores). */
    private long nextBoatTryMs;
    private long ticks;
    private String status = "idle";

    public SurvivalModule(AltoClef mod) {
        this.mod = mod;
        gateChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus gear gates", 51f);
        cautionChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus caution", 53f);
        boatChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus boat", 52.5f);
        stashChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus stashes", 51.5f);
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
        if (s.userTaskRunning) combat.tick(mod, cfg);
        else combat.stop(mod);
        if (++ticks % DECIDE_EVERY_TICKS != 0) return;
        if (cfg.waterClutch) ensureAutoMlg();
        if (!s.userTaskRunning || s.mobDefenseActive || s.screenOpen || s.dead) return;

        long now = System.currentTimeMillis();
        // A gate job that ended without finishing (timed out or cancelled) counts as a failed attempt.
        if (runningGate != null && !gateChain.isActive()) failGate("did not finish");

        if (!cautionChain.isActive()) {
            CautionPolicy.Action a = caution.decide(s.health, s.hostilesNear16, s.y, cfg);
            if (a == CautionPolicy.Action.HOLD_AND_EAT) {
                cautionChain.startJob(new RestTask(), "Low health: resting and eating", null);
            } else if (a == CautionPolicy.Action.FLEE) {
                cautionChain.startJob(new RunAwayFromHostilesTask(24, true), "Low health: getting away from mobs", null);
            }
        }
        if (cautionChain.isActive()) {
            status = cautionChain.jobLabel();
            return;
        }

        String cmd = Plus.control().lastCommand();
        if (!cmd.equals(lastCommand)) {
            stashes.reset();
            lastCommand = cmd;
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
        Optional<GatePlanner.Need> need = gates.next(s, cfg, now);
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
        gateChain.cancelJob();
        stashChain.cancelJob();
        boatChain.cancelJob();
        cautionChain.cancelJob();
        combat.stop(mod);
    }
}
