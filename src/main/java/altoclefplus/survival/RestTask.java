package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.time.TimerGame;
import altoclefplus.Plus;

/**
 * Stand still and heal. AltoClef's food chain eats on its own when health is low, and natural regeneration
 * does the rest. Ends at the resume health, or after 90 seconds so the bot never stalls for good.
 */
public final class RestTask extends Task {
    private final TimerGame timeout = new TimerGame(90);

    @Override
    protected void onStart(AltoClef mod) {
        timeout.reset();
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        setDebugState("Resting to heal (" + Math.round(mod.getPlayer().getHealth()) + " hp)");
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return mod.getPlayer() == null
                || mod.getPlayer().getHealth() >= Plus.config().survival.resumeHealth
                || timeout.elapsed();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof RestTask;
    }

    @Override
    protected String toDebugString() {
        return "Resting to heal";
    }
}
