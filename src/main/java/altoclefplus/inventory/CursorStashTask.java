package altoclefplus.inventory;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.slot.EnsureFreeCursorSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.time.TimerGame;

/**
 * Puts away an item stuck on the cursor using AltoClef's own logic. AltoClef's task never reports itself
 * finished, so this wrapper ends as soon as the cursor is empty (or after 5 seconds).
 */
public final class CursorStashTask extends Task {
    private final TimerGame limit = new TimerGame(5);

    @Override
    protected void onStart(AltoClef mod) {
        limit.reset();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        setDebugState("Putting away a stuck cursor item");
        return new EnsureFreeCursorSlotTask();
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return limit.elapsed() || mod.getPlayer() == null
                || mod.getPlayer().currentScreenHandler.getCursorStack().isEmpty();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof CursorStashTask;
    }

    @Override
    protected String toDebugString() {
        return "Put away cursor item";
    }
}
