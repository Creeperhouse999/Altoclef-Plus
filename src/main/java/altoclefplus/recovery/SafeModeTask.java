package altoclefplus.recovery;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.time.TimerGame;
import altoclefplus.Plus;
import net.minecraft.world.World;

/**
 * Safe mode after repeated failed recoveries: climb out of caves in the Overworld, then stand still, eat and
 * heal for a while before the task is retried. Never runs longer than two minutes.
 */
public final class SafeModeTask extends Task {
    private static final int SURFACE_Y = 70;
    private final TimerGame rest = new TimerGame(30);
    private final TimerGame limit = new TimerGame(120);
    private boolean resting;

    @Override
    protected void onStart(AltoClef mod) {
        limit.reset();
        resting = false;
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        boolean overworld = mod.getWorld().getRegistryKey() == World.OVERWORLD;
        if (!resting && overworld && mod.getPlayer().getY() < SURFACE_Y - 15) {
            setDebugState("Safe mode: getting out of the cave");
            return new GetToYTask(SURFACE_Y);
        }
        if (!resting) {
            resting = true;
            rest.reset();
        }
        setDebugState("Safe mode: resting");
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        if (limit.elapsed()) return true;
        return resting && rest.elapsed() && mod.getPlayer().getHealth() >= Plus.config().survival.resumeHealth;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SafeModeTask;
    }

    @Override
    protected String toDebugString() {
        return "Safe mode";
    }
}
