package altoclefplus.recovery;

import adris.altoclef.AltoClef;
import adris.altoclef.Settings;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.util.ItemTarget;
import altoclefplus.Plus;
import altoclefplus.core.PlusChain;
import altoclefplus.core.logic.Module;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.core.logic.events.Notice;
import altoclefplus.core.logic.events.RecoveryAction;
import altoclefplus.core.logic.events.StuckDetected;
import altoclefplus.recovery.logic.Detector;
import altoclefplus.recovery.logic.DimensionPingPongDetector;
import altoclefplus.recovery.logic.EscalationLadder;
import altoclefplus.recovery.logic.FlipFlopDetector;
import altoclefplus.recovery.logic.NoProgressDetector;
import altoclefplus.recovery.logic.PillarDetector;
import altoclefplus.recovery.logic.RestartLoopDetector;
import altoclefplus.recovery.logic.SameBlockDetector;
import altoclefplus.recovery.logic.StuckKind;
import altoclefplus.survival.logic.DeathLoopGuard;
import altoclefplus.survival.logic.DeathRecoveryPlanner;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalGetToBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Stuck detection + escalation, and going back for items after a death. */
public final class RecoveryModule implements Module {
    private static final double RECOVERING_SHOWN_SECONDS = 30;

    private final AltoClef mod;
    private final PlusChain chain;
    private final List<Detector> detectors;
    private final EscalationLadder ladder;
    private final DeathLoopGuard deathLoops = new DeathLoopGuard();
    /** Pillagers within 32 blocks when the bot died (they shoot from range, so one death is enough to stay away). */
    private int pillagersAtDeath;
    private Snapshot prev = Snapshot.builder().build();
    private double lastActionSec = Double.NEGATIVE_INFINITY;
    private String status = "watching";

    // death recovery state
    private BlockPos deathPos;
    private String deathDim = "";
    private double deathY;
    private String deathCause = "";
    private Set<Item> lostItems = Set.of();
    private boolean botWasRunning;
    private long deathTick;

    public RecoveryModule(AltoClef mod) {
        this.mod = mod;
        PlusConfig.Recovery cfg = Plus.config().recovery;
        chain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus recovery", 54f, 4 * 60_000L);
        detectors = List.of(new NoProgressDetector(cfg), new PillarDetector(cfg), new SameBlockDetector(cfg),
                new FlipFlopDetector(cfg), new RestartLoopDetector(cfg), new DimensionPingPongDetector(cfg));
        ladder = new EscalationLadder(cfg);
    }

    @Override
    public String id() {
        return "recovery";
    }

    @Override
    public void tick(Snapshot s) {
        altoclefplus.zones.Zones.DANGER.setStandingAt(s.dimension, (int) Math.floor(s.x), (int) Math.floor(s.y), (int) Math.floor(s.z));
        handleDeath(s);
        prev = s;
        if (chain.isActive()) {
            status = chain.jobLabel();
            return;
        }
        if (s.seconds() - lastActionSec > RECOVERING_SHOWN_SECONDS) {
            Plus.control().setRecovering(false);
            status = "watching";
        }
        // Our own side jobs (resting, crafting a gate item, picking up drops...) are not "stuck".
        if (mod.getTaskRunner().getCurrentTaskChain() instanceof PlusChain) return;

        boolean anyStuck = false;
        for (Detector d : detectors) {
            var fired = d.update(s);
            if (fired.isEmpty()) continue;
            anyStuck = true;
            Plus.bus().publish(new StuckDetected(d.kind().name(), fired.get()));
            ladder.onStuck(s.seconds()).ifPresent(level -> act(level, d.kind(), s));
            break;
        }
        if (!anyStuck && s.userTaskRunning) ladder.onProgress(s.seconds());
    }

    private void act(EscalationLadder.Level level, StuckKind kind, Snapshot s) {
        lastActionSec = s.seconds();
        Plus.control().setRecovering(true);
        String what;
        switch (level) {
            case REPATH -> {
                mod.getClientBaritone().getPathingBehavior().cancelEverything();
                what = "finding a new path";
            }
            case BLACKLIST -> what = avoid(kind, s);
            case RESTART -> {
                Plus.control().restart();
                what = "restarting the task";
            }
            case ESCAPE -> {
                chain.startJob(new altoclefplus.core.StartedFirstTask(new TimeoutWanderTask(35)), "Stuck: walking away to try again", null);
                what = "walking away and trying again";
            }
            case SAFE_MODE -> {
                chain.startJob(new SafeModeTask(), "Safe mode: getting safe and healing", null);
                Plus.bus().publish(new Notice("recovery", "Safe mode: nothing worked, resting before retrying"));
                what = "safe mode";
                if (ladder.shouldStopBot()) {
                    Plus.control().stop();
                    what = "stopping the bot (setting: stop after safe modes)";
                }
            }
            default -> what = "";
        }
        if (kind == StuckKind.DIMENSION_PING_PONG) {
            disableNetherFastTravel();
            what += "; walking instead of using the Nether";
        }
        status = kind.label + ": " + what;
        Plus.bus().publish(new RecoveryAction(level.name(), "Stuck (" + kind.label + "): " + what));
    }

    /** Level 2: make AltoClef pick another target. */
    private String avoid(StuckKind kind, Snapshot s) {
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
        if (kind == StuckKind.SAME_BLOCK && s.breakingPos != Snapshot.NO_POS) {
            mod.getBlockTracker().requestBlockUnreachable(BlockPos.fromLong(s.breakingPos), 1);
            return "skipping that block";
        }
        if (kind == StuckKind.PILLAR) {
            var p = mod.getPlayer();
            boolean sky = p != null && p.world.isSkyVisible(p.getBlockPos().up());
            var target = altoclefplus.recovery.logic.PillarRecovery.descendTo(s.y, s.dimension, sky);
            if (target.isPresent()) {
                chain.startJob(new GetToYTask(target.getAsInt()), "Stuck pillaring: getting back down", null);
                return "getting back down";
            }
            chain.startJob(new altoclefplus.core.StartedFirstTask(new TimeoutWanderTask(20)), "Stuck pillaring: walking away", null);
            return "walking away";
        }
        Goal goal = mod.getClientBaritone().getPathingBehavior().getGoal();
        BlockPos target = goal instanceof GoalBlock g ? g.getGoalPos() : goal instanceof GoalGetToBlock g2 ? g2.getGoalPos() : null;
        if (target != null) {
            mod.getBlockTracker().requestBlockUnreachable(target, 1);
            return "avoiding that spot";
        }
        return "finding a new path";
    }

    private static Field fastTravelField;

    /** In memory only: make AltoClef walk instead of going through the Nether for the rest of the session. */
    private void disableNetherFastTravel() {
        Settings settings = mod.getModSettings();
        try {
            if (fastTravelField == null) {
                fastTravelField = Settings.class.getDeclaredField("netherFastTravelWalkingRange");
                fastTravelField.setAccessible(true);
            }
            fastTravelField.setInt(settings, 1_000_000);
        } catch (ReflectiveOperationException e) {
            Plus.log().add("recovery", "Could not turn off Nether fast travel: " + e.getMessage());
        }
    }

    // ---------------- death recovery ----------------

    private void handleDeath(Snapshot s) {
        if (s.dead && !prev.dead) onDeath(s);
        else if (!s.dead && prev.dead && deathPos != null) onRespawn(s);
        if (deathPos != null && !chain.isActive() && DeathRecoveryPlanner.expired(s.tick - deathTick)) deathPos = null;
    }

    private void onDeath(Snapshot s) {
        botWasRunning = prev.userTaskRunning;
        deathPos = new BlockPos(Math.floor(prev.x), Math.floor(prev.y), Math.floor(prev.z));
        deathDim = prev.dimension;
        deathY = prev.y;
        deathTick = s.tick;
        var p = Plus.mod().getPlayer();
        pillagersAtDeath = p == null || p.world == null ? 0 : p.world.getEntitiesByClass(
                net.minecraft.entity.mob.PillagerEntity.class, p.getBoundingBox().expand(32), e -> e.isAlive()).size();
        double bottom = prev.dimension.equals("overworld") ? -64 : 0;
        deathCause = prev.inLava ? "lava" : prev.y < bottom ? "void" : "other";
        Set<Item> items = new HashSet<>();
        for (String id : prev.items.keySet()) items.add(Registry.ITEM.get(new Identifier("minecraft", id)));
        for (Map.Entry<String, String> a : prev.armor.entrySet()) {
            if (!a.getValue().isEmpty()) items.add(Registry.ITEM.get(new Identifier("minecraft", a.getValue())));
        }
        items.remove(Items.AIR);
        lostItems = items;
        // Only deaths during a task count towards "keeps dying here"; idle/AFK deaths are not the bot's route.
        if (botWasRunning) deathLoops.record(System.currentTimeMillis(), prev.x, prev.z);
        Plus.log().add("recovery", "Died at " + deathPos.toShortString() + " (" + deathDim + ", " + deathCause + ")");
    }

    private void onRespawn(Snapshot s) {
        PlusConfig.Survival cfg = Plus.config().survival;
        if (!Plus.config().modules.recovery || !botWasRunning) {
            deathPos = null;
            return;
        }
        // Checked before "anything to pick up?": a bot that keeps dying with empty pockets (e.g. respawning
        // next to a pillager outpost) is exactly the case that needs it. Ranged mobs shoot from 16+ blocks,
        // so the avoided area covers the whole loop radius.
        boolean loop = deathLoops.isLoop(Plus.config().recovery, System.currentTimeMillis());
        int avoid = altoclefplus.recovery.logic.DeathDanger.avoidRadius(pillagersAtDeath, loop, Plus.config().recovery.deathLoopRadius);
        if (avoid > 0) {
            altoclefplus.zones.Zones.DANGER.add(deathDim, deathPos.getX(), deathPos.getY(), deathPos.getZ(),
                    avoid, 10 * 60_000L, System.currentTimeMillis());
            Plus.bus().publish(new Notice("recovery", (pillagersAtDeath > 0 ? "Killed near pillagers at " : "Keeps dying near ")
                    + deathPos.toShortString() + " - avoiding that area for 10 minutes"));
            deathPos = null;
            return;
        }
        if (lostItems.isEmpty()) {
            deathPos = null;
            return;
        }
        double dist = Math.hypot(s.x - deathPos.getX(), s.z - deathPos.getZ());
        DeathRecoveryPlanner.Plan plan = DeathRecoveryPlanner.plan(deathCause, s.dimension.equals(deathDim), dist, deathY, cfg);
        BlockPos pos = deathPos;
        Set<Item> lost = lostItems;
        int radius = cfg.deathPickupRadius;
        switch (plan) {
            case SKIP -> {
                Plus.log().add("recovery", "Not going back for items (" + deathCause + ")");
                deathPos = null;
            }
            case GO -> chain.startJob(new DeathLootTask(pos, lost, radius), "Going back for items (" + Math.round(dist) + " blocks)", null);
            case KIT_THEN_GO -> chain.startJob(
                    TaskCatalogue.getSquashedItemTask(new ItemTarget(Items.WOODEN_PICKAXE, 1), new ItemTarget(Items.WOODEN_SWORD, 1)),
                    "Quick tools before going back for items",
                    () -> chain.startJob(new DeathLootTask(pos, lost, radius), "Going back for items (" + Math.round(dist) + " blocks)", null));
        }
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public void onDisable() {
        chain.cancelJob();
        Plus.control().setRecovering(false);
    }
}
