package altoclefplus.mixin;

import altoclefplus.survival.DoorRules;
import altoclefplus.survival.logic.MoveCells;
import baritone.pathing.movement.CalculationContext;
import baritone.pathing.movement.movements.MovementDescend;
import baritone.utils.pathing.MutableMoveResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Door fix: step-down moves never go through a door or fence gate (only the straight move opens them). */
@Mixin(value = MovementDescend.class, remap = false)
public abstract class DescendDoorMixin {
    @Inject(method = "cost(Lbaritone/pathing/movement/CalculationContext;IIIIILbaritone/utils/pathing/MutableMoveResult;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$noDoors(CalculationContext ctx, int x, int y, int z, int destX, int destZ,
                                             MutableMoveResult res, CallbackInfo ci) {
        if (DoorRules.enabled() && DoorRules.touchesDoor(ctx, MoveCells.vertical(x, y, z, destX, destZ))) ci.cancel();
    }
}
