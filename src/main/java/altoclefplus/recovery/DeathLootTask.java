package altoclefplus.recovery;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Set;

/** Walk back to where the bot died and pick its items up again. */
public final class DeathLootTask extends Task {
    private final BlockPos deathPos;
    private final Set<Item> lost;
    private final ItemTarget[] targets;
    private final int radius;

    public DeathLootTask(BlockPos deathPos, Set<Item> lost, int radius) {
        this.deathPos = deathPos;
        this.lost = lost;
        this.radius = radius;
        this.targets = lost.stream().map(i -> new ItemTarget(i, 1)).toArray(ItemTarget[]::new);
    }

    @Override
    protected void onStart(AltoClef mod) {
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (!mod.getPlayer().getBlockPos().isWithinDistance(deathPos, 6)) {
            setDebugState("Going back to death spot " + deathPos.toShortString());
            return new GetToBlockTask(deathPos);
        }
        setDebugState("Picking items back up");
        return new PickupDroppedItemTask(targets, true);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        if (mod.getPlayer() == null || !mod.getPlayer().getBlockPos().isWithinDistance(deathPos, radius)) return false;
        List<ItemEntity> left = mod.getWorld().getEntitiesByClass(ItemEntity.class, new Box(deathPos).expand(radius),
                e -> lost.contains(e.getStack().getItem()));
        return left.isEmpty();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof DeathLootTask d && d.deathPos.equals(deathPos);
    }

    @Override
    protected String toDebugString() {
        return "Recover items after death";
    }
}
