package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.Settings;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import altoclefplus.Plus;
import altoclefplus.core.PlusChain;
import altoclefplus.core.logic.Module;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.core.logic.events.StuckDetected;
import altoclefplus.survival.logic.CautionPolicy;
import altoclefplus.survival.logic.GatePlanner;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Field;
import java.util.Optional;

/** Progression gates, caution, shield / dodging and the water-bucket clutch setting. */
public final class SurvivalModule implements Module {
    private static final int DECIDE_EVERY_TICKS = 20;

    private final AltoClef mod;
    private final PlusChain gateChain;
    private final PlusChain cautionChain;
    private final GatePlanner gates = new GatePlanner();
    private final CautionPolicy caution = new CautionPolicy();
    private final CombatAssist combat = new CombatAssist();
    private GatePlanner.Gate runningGate;
    private long ticks;
    private String status = "idle";

    public SurvivalModule(AltoClef mod) {
        this.mod = mod;
        gateChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus gear gates", 51f);
        cautionChain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus caution", 53f);
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

        if (gateChain.isActive()) {
            status = gateChain.jobLabel();
            return;
        }
        if (!"overworld".equals(s.dimension)) {
            status = "idle";
            return;
        }
        Optional<GatePlanner.Need> need = gates.next(s, cfg, now);
        if (need.isEmpty()) {
            status = "idle";
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
        cautionChain.cancelJob();
        combat.stop(mod);
    }
}
