package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import altoclefplus.survival.logic.WaterSafety;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Low on air underwater: swim straight up when the surface is close, otherwise place a door next to us (doors
 * can't hold water, so its cells become air) and step into it to breathe. Ends when air is nearly full.
 */
public final class SurfaceForAirTask extends Task {
    private static final int FULL_AIR = 290;

    private WaterSafety.AirAction mode;
    private BlockPos doorCell;
    private final TimerGame timeout = new TimerGame(30);

    public SurfaceForAirTask(WaterSafety.AirAction mode) {
        this.mode = mode;
    }

    @Override
    protected void onStart(AltoClef mod) {
        timeout.reset();
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        ClientPlayerEntity p = mod.getPlayer();
        if (mode == WaterSafety.AirAction.DOOR_POCKET && !tickDoor(mod, p)) mode = WaterSafety.AirAction.SWIM_UP;
        if (mode == WaterSafety.AirAction.SWIM_UP) {
            setDebugState("Low on air: swimming up");
            mod.getInputControls().forceLook(p.getYaw(), -90f);
            mod.getInputControls().tryPress(Input.JUMP);
        }
        return null;
    }

    /** False if a door pocket can't be made here. */
    private boolean tickDoor(AltoClef mod, ClientPlayerEntity p) {
        World w = p.world;
        if (doorCell == null) {
            doorCell = findDoorCell(w, p.getBlockPos());
            if (doorCell == null) return false;
        }
        BlockState at = w.getBlockState(doorCell);
        if (!(at.getBlock() instanceof DoorBlock)) {
            Item door = carriedDoor(mod);
            if (door == null || !mod.getSlotHandler().forceEquipItem(door)) return false;
            setDebugState("Low on air: placing a door for an air pocket");
            LookHelper.lookAt(mod, Vec3d.ofCenter(doorCell.down()).add(0, 0.5, 0));
            mod.getInputControls().tryPress(Input.CLICK_RIGHT);
            return true;
        }
        if (p.getBlockPos().equals(doorCell)) {
            setDebugState("Breathing in the door's air pocket");
            return true;
        }
        if (!at.get(DoorBlock.OPEN)) {
            LookHelper.lookAt(mod, Vec3d.ofCenter(doorCell));
            mod.getInputControls().tryPress(Input.CLICK_RIGHT);
            return true;
        }
        setDebugState("Low on air: stepping into the door");
        Vec3d center = Vec3d.ofBottomCenter(doorCell);
        double dx = center.x - p.getX();
        double dz = center.z - p.getZ();
        mod.getInputControls().forceLook((float) (MathHelper.atan2(dz, dx) * 180 / Math.PI) - 90f, 0f);
        mod.getInputControls().tryPress(Input.MOVE_FORWARD);
        return true;
    }

    /** A water cell beside us with solid ground under it and room for a door (2 cells). */
    private static BlockPos findDoorCell(World w, BlockPos feet) {
        for (Direction d : Direction.Type.HORIZONTAL) {
            BlockPos c = feet.offset(d);
            if (isWater(w.getBlockState(c)) && (isWater(w.getBlockState(c.up())) || w.isAir(c.up()))
                    && w.getBlockState(c.down()).isSideSolidFullSquare(w, c.down(), Direction.UP)) {
                return c;
            }
        }
        return null;
    }

    private static boolean isWater(BlockState s) {
        return s.getFluidState().getFluid() == Fluids.WATER;
    }

    static Item carriedDoor(AltoClef mod) {
        for (Item door : ItemHelper.WOOD_DOOR) if (mod.getItemStorage().hasItem(door)) return door;
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getInputControls().release(Input.JUMP);
        mod.getInputControls().release(Input.MOVE_FORWARD);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        ClientPlayerEntity p = mod.getPlayer();
        return p == null || p.getAir() >= FULL_AIR || timeout.elapsed();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SurfaceForAirTask;
    }

    @Override
    protected String toDebugString() {
        return "Getting air";
    }
}
