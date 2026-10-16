package altoclefplus.ui;

import altoclefplus.Plus;
import altoclefplus.core.logic.Module;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.core.logic.events.Notice;
import altoclefplus.core.logic.events.RecoveryAction;
import altoclefplus.core.logic.events.StuckDetected;
import altoclefplus.ui.logic.PlanTracker;
import altoclefplus.ui.logic.StepTrail;

/** Keys, plan tracking, step trail and the HUD alert line. */
public final class UiModule implements Module {
    public static final long ALERT_MS = 5000;

    private final PlanTracker plan;
    private final StepTrail trail = new StepTrail(50);
    private String alert = "";
    private long alertAtMs;
    private String lastCommand = "";

    public UiModule(int targetEyes) {
        this(targetEyes, 1);
    }

    public UiModule(int targetEyes, int targetBeds) {
        plan = new PlanTracker(targetEyes, targetBeds);
        plan.setListener(m -> Plus.log().add("plan", "Milestone reached: " + m.label));
        Plus.bus().subscribe(StuckDetected.class, e -> alert("Stuck (" + e.kind().toLowerCase().replace('_', ' ') + ")"));
        Plus.bus().subscribe(RecoveryAction.class, e -> alert(e.action()));
        Plus.bus().subscribe(Notice.class, e -> alert(e.text()));
    }

    @Override
    public String id() {
        return "ui";
    }

    @Override
    public void tick(Snapshot s) {
        String cmd = Plus.control().lastCommand();
        if (!cmd.equals(lastCommand)) {
            if (cmd.startsWith("gamer")) plan.reset();
            if (!cmd.isEmpty()) trail.clear();
            lastCommand = cmd;
        }
        plan.setRequireWaterBucket(altoclefplus.Plus.config().survival.gateWaterBucket);
        plan.update(s);
        if (s.userTaskRunning && !s.taskChain.isEmpty()) {
            trail.update(s.taskChain.get(s.taskChain.size() - 1), System.currentTimeMillis());
        }
    }

    @Override
    public String status() {
        return "ok";
    }

    public void alert(String text) {
        alert = text;
        alertAtMs = System.currentTimeMillis();
        if (Plus.config().recovery.logActions) Plus.log().add("plus", text);
    }

    /** Current alert text and its age; empty when expired. */
    public String activeAlert() {
        return System.currentTimeMillis() - alertAtMs < ALERT_MS ? alert : "";
    }

    public float alertFade() {
        long age = System.currentTimeMillis() - alertAtMs;
        return Math.max(0f, 1f - (float) age / ALERT_MS);
    }

    public PlanTracker plan() {
        return plan;
    }

    public StepTrail trail() {
        return trail;
    }
}
