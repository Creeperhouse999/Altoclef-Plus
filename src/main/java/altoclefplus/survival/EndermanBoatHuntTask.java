package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import altoclefplus.Plus;
import altoclefplus.inventory.HotbarManager;
import altoclefplus.survival.logic.CombatAim;
import altoclefplus.survival.logic.CritTiming;
import altoclefplus.survival.logic.EndermanHuntPlanner;
import baritone.api.utils.input.Input;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Comparator;
import java.util.List;

/**
 * Pearls the reliable way: in a warped forest, put a boat down next to the bot, stare at an enderman so it
 * comes over, and when it sits in the boat (endermen can't teleport out of boats) kill it with jump
 * critical hits. If the trap doesn't work in 20 seconds, or there's no boat, it just fights with crits.
 */
public final class EndermanBoatHuntTask extends Task {
    private static final double REACH = 3.0;

    private final int target;
    private long aggroSinceMs = -1;
    private int placeCooldown;

    public EndermanBoatHuntTask(int targetPearls) {
        this.target = targetPearls;
    }

    @Override
    protected void onStart(AltoClef mod) {
        aggroSinceMs = -1;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        ClientPlayerEntity p = mod.getPlayer();
        World world = mod.getWorld();
        if (placeCooldown > 0) placeCooldown--;

        List<EndermanEntity> endermen = world.getEntitiesByClass(EndermanEntity.class, p.getBoundingBox().expand(24),
                e -> e.isAlive());
        EndermanEntity seatedOne = endermen.stream().filter(e -> e.getVehicle() instanceof BoatEntity)
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
        EndermanEntity nearest = seatedOne != null ? seatedOne
                : endermen.stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
        BoatEntity boat = world.getEntitiesByClass(BoatEntity.class, p.getBoundingBox().expand(6), b -> true)
                .stream().min(Comparator.comparingDouble(b -> b.squaredDistanceTo(p))).orElse(null);
        boolean angry = nearest != null && nearest.isAngry();
        long now = System.currentTimeMillis();
        if (angry && aggroSinceMs < 0) aggroSinceMs = now;
        if (!angry) aggroSinceMs = -1;

        EndermanHuntPlanner.State state = new EndermanHuntPlanner.State(
                mod.getItemStorage().getItemCount(Items.ENDER_PEARL), target,
                world.getRegistryKey() == World.NETHER,
                world.getBiome(p.getBlockPos()).matchesKey(BiomeKeys.WARPED_FOREST),
                boatItem() != null, boat != null, nearest != null, seatedOne != null, angry,
                aggroSinceMs < 0 ? 0 : (now - aggroSinceMs) / 1000.0,
                mod.getEntityTracker().itemDropped(Items.ENDER_PEARL));

        switch (EndermanHuntPlanner.next(state)) {
            case DONE -> {
                return null;
            }
            case COLLECT -> {
                setDebugState("Picking up pearls");
                return new PickupDroppedItemTask(Items.ENDER_PEARL, target);
            }
            case GO_TO_NETHER -> {
                setDebugState("Going to the Nether for endermen");
                return new DefaultGoToDimensionTask(Dimension.NETHER);
            }
            case FIND_FOREST -> {
                setDebugState("Looking for a warped forest");
                return new SearchChunkForBlockTask(Blocks.WARPED_NYLIUM);
            }
            case PLACE_BOAT -> {
                setDebugState("Placing boat trap");
                placeBoat(mod, p);
                return null;
            }
            case AGGRO -> {
                setDebugState("Looking at an enderman to lure it");
                if (!p.canSee(nearest)) return new GetToEntityTask(nearest, 10);
                look(p, nearest.getEyePos());
                return null;
            }
            case WAIT -> {
                setDebugState("Waiting by the boat");
                if (boat != null && boat.squaredDistanceTo(p) > 2.5 * 2.5) return new GetToBlockTask(boat.getBlockPos());
                if (nearest != null) look(p, nearest.getBoundingBox().getCenter());
                return null;
            }
            case FIGHT -> {
                setDebugState(seatedOne != null ? "Enderman trapped - critical hits" : "Fighting enderman");
                return fight(mod, p, nearest);
            }
        }
        return null;
    }

    private Task fight(AltoClef mod, ClientPlayerEntity p, EndermanEntity enderman) {
        if (enderman == null) return null;
        String weapon = CritTiming.bestWeapon(Plus.snapshot().items);
        if (!weapon.isEmpty()) HotbarManager.equipItem(Registry.ITEM.get(new Identifier("minecraft", weapon)));
        double dist = p.getEyePos().distanceTo(enderman.getBoundingBox().getCenter());
        if (dist > REACH) return new GetToEntityTask(enderman, 2);
        look(p, enderman.getBoundingBox().getCenter());
        float cooldown = p.getAttackCooldownProgress(0.5f);
        if (CritTiming.shouldJump(p.isOnGround(), cooldown, dist, REACH)) {
            mod.getInputControls().tryPress(Input.JUMP);
        } else if (CritTiming.shouldAttack(p.isOnGround(), p.getVelocity().y, cooldown, dist, REACH)) {
            MinecraftClient.getInstance().interactionManager.attackEntity(p, enderman);
            p.swingHand(Hand.MAIN_HAND);
        }
        return null;
    }

    /** Boat on the ground two blocks ahead (open air above), placed by using the boat item while looking at it. */
    private void placeBoat(AltoClef mod, ClientPlayerEntity p) {
        if (placeCooldown > 0) return;
        Item boat = boatItem();
        if (boat == null || !HotbarManager.equipItem(boat)) return;
        Direction facing = p.getHorizontalFacing();
        BlockPos ground = p.getBlockPos().offset(facing, 2).down();
        World w = p.world;
        if (!w.getBlockState(ground).isSolidBlock(w, ground) || !w.isAir(ground.up()) || !w.isAir(ground.up(2))) {
            p.setYaw(p.getYaw() + 90); // try another side next time
            placeCooldown = 10;
            return;
        }
        look(p, new Vec3d(ground.getX() + 0.5, ground.getY() + 1.0, ground.getZ() + 0.5));
        MinecraftClient.getInstance().interactionManager.interactItem(p, w, Hand.MAIN_HAND);
        placeCooldown = 20;
    }

    private static Item boatItem() {
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
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return mod.getItemStorage().getItemCount(Items.ENDER_PEARL) >= target;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof EndermanBoatHuntTask t && t.target == target;
    }

    @Override
    protected String toDebugString() {
        return "Hunting endermen with a boat trap (" + target + " pearls)";
    }
}
