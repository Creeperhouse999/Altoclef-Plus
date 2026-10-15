package altoclefplus.inventory;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import altoclefplus.Plus;
import altoclefplus.core.StateSampler;
import altoclefplus.core.logic.events.Notice;
import altoclefplus.inventory.logic.JunkPit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Throws junk where the bot won't pick it up again: into lava it can see nearby, otherwise into a 1x1 hole,
 * 3 deep, dug right beside it. Throwing at its own feet meant walking over the junk and picking it back up.
 * Gives up after 30 seconds and drops the rest the old way.
 */
public final class JunkDisposalTask extends Task {
    private final List<String> junk;
    private final TimerGame timeout = new TimerGame(30);
    private BlockPos target; // lava block, or the top of the pit column
    private boolean lava;
    private int lookedTicks;

    public JunkDisposalTask(List<String> junkIds) {
        this.junk = new ArrayList<>(junkIds);
    }

    @Override
    protected void onStart(AltoClef mod) {
        timeout.reset();
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        ClientPlayerEntity p = mod.getPlayer();
        World w = mod.getWorld();
        if (timeout.elapsed()) {
            dropAllHere(p);
            return null;
        }
        if (target == null) pickTarget(p, w);
        if (target == null) {
            dropAllHere(p);
            return null;
        }
        if (!lava) {
            // Dig the pit from the top down until it is open.
            for (int i = 0; i < JunkPit.DEPTH; i++) {
                BlockPos b = target.down(i);
                if (!w.isAir(b)) {
                    setDebugState("Digging a hole for junk");
                    lookedTicks = 0;
                    return new DestroyBlockTask(b);
                }
            }
        }
        Vec3d aim = lava ? Vec3d.ofCenter(target) : Vec3d.ofCenter(target.down(JunkPit.DEPTH - 1)).add(0, -0.3, 0);
        LookHelper.lookAt(mod, aim);
        // Throw on the next ticks, once the server has our new look direction.
        if (++lookedTicks < 3) return null;
        setDebugState(lava ? "Throwing junk into lava" : "Throwing junk into the hole");
        throwOne(p);
        lookedTicks = 1;
        return null;
    }

    private void pickTarget(ClientPlayerEntity p, World w) {
        BlockPos feet = p.getBlockPos();
        // Lava we can see within 6 blocks.
        for (BlockPos b : BlockPos.iterateOutwards(feet, 6, 3, 6)) {
            BlockState s = w.getBlockState(b);
            if (s.isOf(Blocks.LAVA) && clearSight(p, Vec3d.ofCenter(b))) {
                target = b.toImmutable();
                lava = true;
                return;
            }
        }
        JunkPit.World view = (x, y, z) -> {
            BlockPos b = new BlockPos(x, y, z);
            BlockState s = w.getBlockState(b);
            if (s.isAir()) return JunkPit.Cell.AIR;
            if (!s.getFluidState().isEmpty()) return JunkPit.Cell.FLUID;
            if (s.getHardness(w, b) < 0 || w.getBlockEntity(b) != null || !altoclefplus.zones.Zones.mayBreak(b)) return JunkPit.Cell.KEEP;
            return JunkPit.Cell.SOLID;
        };
        JunkPit.choose(view, feet.getX(), feet.getY(), feet.getZ()).ifPresent(t -> {
            target = new BlockPos(t[0], t[1], t[2]);
            lava = false;
        });
    }

    private static boolean clearSight(ClientPlayerEntity p, Vec3d to) {
        HitResult hit = p.world.raycast(new RaycastContext(p.getEyePos(), to, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, p));
        return hit.getType() == HitResult.Type.MISS;
    }

    /** Throws one junk stack (never the held slot, named or enchanted items). */
    private void throwOne(ClientPlayerEntity p) {
        int slot = nextJunkSlot(p);
        if (slot < 0) return;
        String id = StateSampler.id(p.getInventory().main.get(slot));
        int window = slot < 9 ? 36 + slot : slot;
        MinecraftClient.getInstance().interactionManager.clickSlot(p.playerScreenHandler.syncId, window, 1, SlotActionType.THROW, p);
        Plus.bus().publish(new Notice("inventory", "Threw away junk: " + id.replace('_', ' ')));
    }

    private void dropAllHere(ClientPlayerEntity p) {
        for (int i = 0; i < 4; i++) throwOne(p);
        junk.clear();
    }

    private int nextJunkSlot(ClientPlayerEntity p) {
        var main = p.getInventory().main;
        int selected = p.getInventory().selectedSlot;
        for (int i = 0; i < main.size(); i++) {
            if (i == selected) continue;
            ItemStack st = main.get(i);
            if (st.isEmpty() || st.hasCustomName() || st.hasEnchantments()) continue;
            if (junk.contains(StateSampler.id(st))) return i;
        }
        return -1;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        ClientPlayerEntity p = mod.getPlayer();
        return p == null || junk.isEmpty() || nextJunkSlot(p) < 0;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof JunkDisposalTask;
    }

    @Override
    protected String toDebugString() {
        return "Throwing away junk";
    }
}
