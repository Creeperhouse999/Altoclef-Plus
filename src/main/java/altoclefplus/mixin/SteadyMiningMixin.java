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
 * a new one only wins when it is much closer, and not more than once every 3 seconds.
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

    @Inject(method = "getClosestTo", at = @At("RETURN"), cancellable = true, require = 0)
    private void altoclefplus$keepTarget(AltoClef mod, Vec3d pos, CallbackInfoReturnable<Optional<Object>> cir) {
        Hooks.hit("steady-mining");
        if (!Plus.config().survival.steadyMining) return;
        long tick = ++altoclefplus$ticks;
        Optional<Object> picked = cir.getReturnValue();
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
