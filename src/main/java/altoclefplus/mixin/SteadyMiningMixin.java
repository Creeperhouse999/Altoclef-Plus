package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.WorldHelper;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.survival.logic.TargetStickiness;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

/**
 * AltoClef re-picks the nearest block every tick and jumps to any block it hasn't tried yet, so while walking
 * to one log/ore it kept switching targets several times a second. Keep the block we're already going for;
 * a new one only wins when it is much closer, and not more than once every 3 seconds. Once it heads for a
 * dropped item it picks it up before going back to blocks (unless the item can't be reached for 15 s).
 */
@Mixin(targets = "adris.altoclef.tasks.resources.MineAndCollectTask$MineOrCollectTask", remap = false)
public abstract class SteadyMiningMixin {
    private static final long COOLDOWN_TICKS = 60;
    private static Field miningPosField;
    private static Field blacklistField;

    @Unique
    private final TargetStickiness altoclefplus$sticky = new TargetStickiness(COOLDOWN_TICKS);
    @Unique
    private long altoclefplus$ticks;
    /** Dropped item we are walking to: finish picking it up before going back to blocks. */
    @Unique
    private net.minecraft.entity.ItemEntity altoclefplus$chasing;
    @Unique
    private long altoclefplus$chaseStart;
    @Unique
    private int altoclefplus$gaveUpOn = -1;

    @Inject(method = "getClosestTo", at = @At("RETURN"), cancellable = true, require = 0)
    private void altoclefplus$keepTarget(AltoClef mod, Vec3d pos, CallbackInfoReturnable<Optional<Object>> cir) {
        Hooks.hit("steady-mining");
        if (!Plus.config().survival.steadyMining) return;
        long tick = ++altoclefplus$ticks;
        Optional<Object> picked = cir.getReturnValue();
        // Items: AltoClef flipped between "pick up that drop" and "mine that block" as distances changed.
        if (altoclefplus$chasing != null) {
            var item = altoclefplus$chasing;
            boolean stillThere = item.isAlive() && !item.isRemoved() && item.squaredDistanceTo(pos) < 16 * 16;
            if (stillThere && tick - altoclefplus$chaseStart < 300) {
                if (picked == null || picked.isEmpty() || picked.get() != item) cir.setReturnValue(Optional.of(item));
                return;
            }
            if (stillThere) altoclefplus$gaveUpOn = item.getId(); // unreachable for 15 s: let AltoClef decide
            altoclefplus$chasing = null;
        }
        if (picked != null && picked.isPresent() && picked.get() instanceof net.minecraft.entity.ItemEntity item
                && item.getId() != altoclefplus$gaveUpOn) {
            altoclefplus$chasing = item;
            altoclefplus$chaseStart = tick;
            return;
        }
        if (picked == null || picked.isEmpty() || !(picked.get() instanceof BlockPos candidate)) return;
        BlockPos current;
        Set<?> blacklist;
        try {
            if (miningPosField == null) {
                miningPosField = getClass().getDeclaredField("_miningPos");
                blacklistField = getClass().getDeclaredField("_blacklist");
                miningPosField.setAccessible(true);
                blacklistField.setAccessible(true);
            }
            current = (BlockPos) miningPosField.get(this);
            blacklist = (Set<?>) blacklistField.get(this);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return;
        }
        if (current == null || current.equals(candidate)) {
            if (current == null) altoclefplus$sticky.switched(tick);
            return;
        }
        boolean valid = !blacklist.contains(current) && sameBlockStillThere(mod, current, candidate) && WorldHelper.canBreak(mod, current);
        if (altoclefplus$sticky.shouldSwitch(valid, current.getSquaredDistance(pos), candidate.getSquaredDistance(pos), tick)) {
            altoclefplus$sticky.switched(tick);
            return;
        }
        cir.setReturnValue(Optional.of(current));
    }

    /** The block we were going for still exists and is the same kind as the one AltoClef now wants. */
    private static boolean sameBlockStillThere(AltoClef mod, BlockPos current, BlockPos candidate) {
        var world = mod.getWorld();
        return world != null && !world.isAir(current)
                && world.getBlockState(current).getBlock() == world.getBlockState(candidate).getBlock();
    }
}
