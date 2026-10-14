package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.time.TimerGame;
import altoclefplus.Plus;
import altoclefplus.inventory.HotbarManager;
import altoclefplus.survival.logic.BoatTravel;
import altoclefplus.survival.logic.CombatAim;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Comparator;

/**
 * Cross a big body of water by boat toward a destination: put the boat on the water, get in, steer with
 * left/right (that's how boats turn), get out at the far shore, break the boat and pick it back up.
 * Baritone then re-plans from the new shore.
 */
public final class BoatCrossTask extends Task {
    private enum Phase { BOARD, SAIL, LANDED, DONE }

    private final BlockPos goal;
    private final TimerGame limit = new TimerGame(180);
    private Phase phase = Phase.BOARD;
    private int cooldown;
    private Item boatItem;

    public BoatCrossTask(BlockPos goal) {
        this.goal = goal;
    }

    @Override
    protected void onStart(AltoClef mod) {
        limit.reset();
        phase = Phase.BOARD;
        boatItem = carriedBoat();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        ClientPlayerEntity p = mod.getPlayer();
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
        if (cooldown > 0) cooldown--;
        if (limit.elapsed()) {
            releaseKeys(mod);
            phase = Phase.DONE;
            return null;
        }

        if (p.getVehicle() instanceof BoatEntity boat) {
            phase = Phase.SAIL;
            setDebugState("Crossing water by boat");
            sail(mod, p, boat);
            return null;
        }

        if (phase == Phase.SAIL) {
            phase = Phase.LANDED;
            releaseKeys(mod);
        }
        if (phase == Phase.LANDED) {
            setDebugState("Picking the boat back up");
            BoatEntity left = nearestBoat(p, 5);
            if (left != null) {
                if (cooldown == 0) {
                    MinecraftClient.getInstance().interactionManager.attackEntity(p, left);
                    p.swingHand(Hand.MAIN_HAND);
                    cooldown = 4;
                }
                return null;
            }
            if (boatItem != null && mod.getEntityTracker().itemDropped(boatItem)) {
                return new PickupDroppedItemTask(boatItem, 1);
            }
            phase = Phase.DONE;
            return null;
        }

        // BOARD: get into a boat on the water, placing ours if needed.
        setDebugState("Getting into a boat");
        BoatEntity free = nearestBoat(p, 4);
        if (free != null && !free.hasPassengers()) {
            if (cooldown == 0) {
                MinecraftClient.getInstance().interactionManager.interactEntity(p, free, Hand.MAIN_HAND);
                cooldown = 10;
            }
            return null;
        }
        if (boatItem == null || cooldown > 0) return null;
        BlockPos water = waterNextTo(p);
        if (water == null || !HotbarManager.equipItem(boatItem)) {
            phase = Phase.DONE;
            return null;
        }
        look(p, new Vec3d(water.getX() + 0.5, water.getY() + 0.9, water.getZ() + 0.5));
        MinecraftClient.getInstance().interactionManager.interactItem(p, p.world, Hand.MAIN_HAND);
        cooldown = 10;
        return null;
    }

    private void sail(AltoClef mod, ClientPlayerEntity p, BoatEntity boat) {
        Vec3d pos = boat.getPos();
        float targetYaw = CombatAim.lookAngles(pos.x, 0, pos.z, goal.getX() + 0.5, 0, goal.getZ() + 0.5)[0];
        int turn = BoatTravel.turnInput(boat.getYaw(), targetYaw, 6f);
        hold(mod, Input.MOVE_LEFT, turn < 0);
        hold(mod, Input.MOVE_RIGHT, turn > 0);
        hold(mod, Input.MOVE_FORWARD, true);

        double rad = Math.toRadians(boat.getYaw());
        double fx = -Math.sin(rad);
        double fz = Math.cos(rad);
        BlockPos ahead = new BlockPos(pos.x + fx * 2, pos.y, pos.z + fz * 2);
        // Shore: no water at the boat's level or just below, two blocks ahead.
        boolean landAhead = !p.world.getFluidState(ahead).isOf(Fluids.WATER) && !p.world.getFluidState(ahead.down()).isOf(Fluids.WATER);
        int remaining = 0;
        for (int i = 1; i <= 4; i++) {
            BlockPos b = new BlockPos(pos.x + fx * i, pos.y - 0.5, pos.z + fz * i);
            if (p.world.getFluidState(b).isOf(Fluids.WATER)) remaining++;
            else break;
        }
        double distGoal = Math.hypot(goal.getX() - pos.x, goal.getZ() - pos.z);
        if (BoatTravel.shouldStop(landAhead, remaining) || distGoal < 3) {
            hold(mod, Input.MOVE_FORWARD, false);
            hold(mod, Input.MOVE_LEFT, false);
            hold(mod, Input.MOVE_RIGHT, false);
            mod.getInputControls().tryPress(Input.SNEAK); // get out
        }
    }

    private static void hold(AltoClef mod, Input input, boolean down) {
        if (down) mod.getInputControls().hold(input);
        else mod.getInputControls().release(input);
    }

    private static void releaseKeys(AltoClef mod) {
        for (Input i : new Input[]{Input.MOVE_FORWARD, Input.MOVE_LEFT, Input.MOVE_RIGHT}) mod.getInputControls().release(i);
    }

    private static BoatEntity nearestBoat(ClientPlayerEntity p, double range) {
        return p.world.getEntitiesByClass(BoatEntity.class, p.getBoundingBox().expand(range), b -> true).stream()
                .min(Comparator.comparingDouble(b -> b.squaredDistanceTo(p))).orElse(null);
    }

    /** A water-surface block within 2 blocks (open air above). */
    private static BlockPos waterNextTo(ClientPlayerEntity p) {
        World w = p.world;
        BlockPos base = p.getBlockPos();
        for (int r = 1; r <= 2; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = -1; dy <= 0; dy++) {
                        BlockPos b = base.add(dx, dy, dz);
                        if (w.getFluidState(b).isOf(Fluids.WATER) && w.isAir(b.up())) return b;
                    }
                }
            }
        }
        return null;
    }

    private static Item carriedBoat() {
        for (String id : Plus.snapshot().items.keySet()) {
            if (id.endsWith("_boat")) {
                Item it = Registry.ITEM.get(new Identifier("minecraft", id));
                if (it instanceof BoatItem) return it;
            }
        }
        return null;
    }

    private static void look(ClientPlayerEntity p, Vec3d at) {
        Vec3d eye = p.getEyePos();
        float[] a = CombatAim.lookAngles(eye.x, eye.y, eye.z, at.x, at.y, at.z);
        p.setYaw(a[0]);
        p.setPitch(a[1]);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        releaseKeys(mod);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return phase == Phase.DONE;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BoatCrossTask t && t.goal.equals(goal);
    }

    @Override
    protected String toDebugString() {
        return "Boat across water";
    }
}
