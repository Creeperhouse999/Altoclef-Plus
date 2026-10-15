package altoclefplus.mixin;

import altoclefplus.core.Hooks;
import altoclefplus.survival.DoorRules;
import altoclefplus.survival.logic.MoveCells;
import baritone.pathing.movement.CalculationContext;
import baritone.pathing.movement.movements.MovementDiagonal;
import baritone.utils.pathing.MutableMoveResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Door fix: Baritone treats wooden doors and fence gates as open space, but only the straight one-block move
 * opens them. Diagonal moves walked into closed doors, timed out and were planned again, leaving the bot
 * standing in front of the door. Diagonals now never pass through door/gate cells (cost stays "impossible").
 */
@Mixin(value = MovementDiagonal.class, remap = false)
public abstract class DiagonalDoorMixin {
    @Inject(method = "cost(Lbaritone/pathing/movement/CalculationContext;IIIIILbaritone/utils/pathing/MutableMoveResult;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$noDoors(CalculationContext ctx, int x, int y, int z, int destX, int destZ,
                                             MutableMoveResult res, CallbackInfo ci) {
        Hooks.hit("door-path");
        if (DoorRules.enabled() && DoorRules.touchesDoor(ctx, MoveCells.diagonal(x, y, z, destX, destZ))) ci.cancel();
    }
}
