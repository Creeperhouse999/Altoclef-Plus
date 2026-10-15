package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import altoclefplus.survival.logic.NightShelter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Digs straight down two blocks, covers the hole with a throwaway block and waits for dawn. Nothing can spawn
 * in or reach a covered 1x1 hole, so a weak early bot survives the night instead of dying to skeletons.
 */
public final class NightShelterTask extends Task {
    private BlockPos top;
    private boolean unsafe;

    @Override
    protected void onStart(AltoClef mod) {
        top = mod.getPlayer().getBlockPos();
        unsafe = !solidFloor(mod, top.down()) || !solidFloor(mod, top.down(2)) || !solidFloor(mod, top.down(3));
    }

    /** Solid, breakable-looking ground: never dig into caves, liquids or bedrock. */
    private static boolean solidFloor(AltoClef mod, BlockPos pos) {
        BlockState s = mod.getWorld().getBlockState(pos);
        return s.isFullCube(mod.getWorld(), pos) && s.getFluidState().isEmpty()
                && s.getBlock() != net.minecraft.block.Blocks.BEDROCK;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        BlockPos bottom = top.down(2);
        if (!mod.getPlayer().getBlockPos().equals(bottom)) {
            setDebugState("Night without armor: digging a shelter");
            return new GetToBlockTask(bottom);
        }
        if (mod.getWorld().getBlockState(top).isAir()) {
            setDebugState("Night without armor: covering the shelter");
            return new PlaceStructureBlockTask(top);
        }
        setDebugState("Waiting out the night in a shelter");
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return unsafe || mod.getPlayer() == null || !NightShelter.isNight(mod.getWorld().getTimeOfDay());
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof NightShelterTask;
    }

    @Override
    protected String toDebugString() {
        return "Night shelter";
    }
}
