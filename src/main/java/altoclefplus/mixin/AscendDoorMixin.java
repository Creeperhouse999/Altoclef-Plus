package altoclefplus.mixin;

import altoclefplus.survival.DoorRules;
import altoclefplus.survival.logic.MoveCells;
import baritone.pathing.movement.CalculationContext;
import baritone.pathing.movement.movements.MovementAscend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Door fix: step-up moves never go through a door or fence gate (only the straight move opens them). */
@Mixin(value = MovementAscend.class, remap = false)
public abstract class AscendDoorMixin {
    @Inject(method = "cost(Lbaritone/pathing/movement/CalculationContext;IIIII)D",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$noDoors(CalculationContext ctx, int x, int y, int z, int destX, int destZ,
                                             CallbackInfoReturnable<Double> cir) {
        if (DoorRules.enabled() && DoorRules.touchesDoor(ctx, MoveCells.vertical(x, y, z, destX, destZ))) {
            cir.setReturnValue(1_000_000.0); // Baritone's COST_INF
        }
    }
}
