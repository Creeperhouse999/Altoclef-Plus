package altoclefplus.inventory;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.ItemEntity;

/** Walk onto one dropped item so it gets picked up. Gives up after 10 seconds. */
public final class PickupDropTask extends Task {
    private final ItemEntity item;
    private final TimerGame timeout = new TimerGame(10);

    public PickupDropTask(ItemEntity item) {
        this.item = item;
    }

    @Override
    protected void onStart(AltoClef mod) {
        timeout.reset();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        setDebugState("Picking up " + item.getStack().getName().getString());
        return new GetToEntityTask(item);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return !item.isAlive() || item.isRemoved() || timeout.elapsed();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof PickupDropTask p && p.item == item;
    }

    @Override
    protected String toDebugString() {
        return "Picking up items";
    }
}
