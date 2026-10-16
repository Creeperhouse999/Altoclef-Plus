package altoclefplus.inventory;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

/**
 * Breaks a crafting table / furnace the bot placed itself and picks it back up, so the next craft places the
 * carried one instead of crafting a new one (see StationTracker). Gives up after 20 seconds.
 */
final class PickUpStationTask extends Task {
    private final BlockPos pos;
    private final Block block;
    private final Item item;
    private final TimerGame timeout = new TimerGame(20);
    private int startCount;

    PickUpStationTask(BlockPos pos, Block block, Item item) {
        this.pos = pos;
        this.block = block;
        this.item = item;
    }

    @Override
    protected void onStart(AltoClef mod) {
        timeout.reset();
        startCount = mod.getItemStorage().getItemCount(item);
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getWorld().getBlockState(pos).isOf(block)) {
            setDebugState("Breaking our " + item.getName().getString().toLowerCase() + " to carry it");
            return new DestroyBlockTask(pos);
        }
        setDebugState("Picking it up");
        return new PickupDroppedItemTask(item, startCount + 1);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return timeout.elapsed() || mod.getItemStorage().getItemCount(item) > startCount;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof PickUpStationTask t && t.pos.equals(pos);
    }

    @Override
    protected String toDebugString() {
        return "Pick up our " + item.getName().getString().toLowerCase() + " at " + pos.toShortString();
    }
}
