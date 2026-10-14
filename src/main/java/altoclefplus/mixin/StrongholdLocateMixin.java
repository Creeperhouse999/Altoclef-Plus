package altoclefplus.mixin;

import adris.altoclef.tasks.movement.LocateStrongholdCoordinatesTask;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.survival.logic.StrongholdTravel;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Stronghold triangulation fixes. AltoClef intersected the throw lines on X/Z but returned a 3D point, so the
 * rising eye produced estimates like y=1620; nearly parallel or backwards throws were never rejected; and the
 * two throws were only 30 blocks apart.
 */
@Mixin(value = LocateStrongholdCoordinatesTask.class, remap = false)
public abstract class StrongholdLocateMixin {
    private static final double MIN_ANGLE_DEG = 1.0;
    private static final double MAX_DISTANCE = 3000;

    /**
     * Valid estimate: X/Z intersection at the second throw's height. Invalid: return the second throw's
     * origin, which sits right next to the player, so AltoClef's own "estimate within 10 blocks" check discards
     * it and throws again.
     */
    @Inject(method = "calculateIntersection", at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$validatedIntersection(Vec3d start1, Vec3d dir1, Vec3d start2, Vec3d dir2,
                                                           CallbackInfoReturnable<Vec3d> cir) {
        Hooks.hit("stronghold-estimate");
        if (!enabled()) return;
        Optional<double[]> p = StrongholdTravel.intersectXZ(start1.x, start1.z, dir1.x, dir1.z,
                start2.x, start2.z, dir2.x, dir2.z, MIN_ANGLE_DEG, MAX_DISTANCE);
        if (p.isPresent()) {
            cir.setReturnValue(new Vec3d(p.get()[0], start2.y, p.get()[1]));
        } else {
            Plus.log().add("survival", "Eye throws didn't give a reliable stronghold position - throwing again");
            cir.setReturnValue(start2);
        }
    }

    /** Second throw 100 blocks to the side instead of 30, for a usable angle between the two lines. */
    @ModifyConstant(method = "onTick", constant = @Constant(doubleValue = 900.0), require = 0)
    private double altoclefplus$widerBaseline(double original) {
        return enabled() ? 10_000.0 : original;
    }

    private static boolean enabled() {
        return Plus.mod() != null && Plus.config().modules.survival && Plus.config().survival.preciseNetherTravel;
    }
}
