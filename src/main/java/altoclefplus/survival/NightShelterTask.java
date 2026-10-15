package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import altoclefplus.survival.logic.NightShelter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Digs straight down three blocks, covers the hole at ground level and waits for dawn. Nothing can spawn
 * in or reach a covered 1x1 hole, so a weak early bot survives the night instead of dying to skeletons.
 * The cover goes in at ground level because there the hole's walls give it something to be placed against;
 * placing it from inside also keeps Baritone from climbing out to do it.
 */
public final class NightShelterTask extends Task {
    private BlockPos cover;
    private boolean unsafe;

    @Override
    protected void onStart(AltoClef mod) {
        BlockPos feet = mod.getPlayer().getBlockPos();
        cover = feet.down();
        unsafe = false;
        for (int d = 1; d <= 4; d++) if (!solid(mod, feet.down(d))) unsafe = true;
    }

    /** Solid ground: never dig into caves, liquids or bedrock. */
    private static boolean solid(AltoClef mod, BlockPos pos) {
        BlockState s = mod.getWorld().getBlockState(pos);
        return s.isFullCube(mod.getWorld(), pos) && s.getFluidState().isEmpty() && s.getBlock() != Blocks.BEDROCK;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        BlockPos bottom = cover.down(2);
        if (!mod.getPlayer().getBlockPos().equals(bottom)) {
            setDebugState("Night without armor: digging a shelter");
            return new GetToBlockTask(bottom);
        }
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
        if (mod.getWorld().getBlockState(cover).isAir()) {
            setDebugState("Night without armor: covering the shelter");
            placeCover(mod);
            return null;
        }
        setDebugState("Waiting out the night in a shelter");
        return null;
    }

    private void placeCover(AltoClef mod) {
        if (!mod.getSlotHandler().forceEquipItem(mod.getModSettings().getThrowawayItems(mod))) return;
        if (!(mod.getPlayer().getMainHandStack().getItem() instanceof BlockItem)) return;
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos wall = cover.offset(dir);
            if (!solid(mod, wall)) continue;
            Direction face = dir.getOpposite();
            LookHelper.lookAt(mod, wall, face);
            Vec3d hit = Vec3d.ofCenter(wall).add(Vec3d.of(face.getVector()).multiply(0.5));
            MinecraftClient.getInstance().interactionManager.interactBlock(mod.getPlayer(), mod.getWorld(), Hand.MAIN_HAND,
                    new BlockHitResult(hit, face, wall, false));
            mod.getPlayer().swingHand(Hand.MAIN_HAND);
            return;
        }
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
