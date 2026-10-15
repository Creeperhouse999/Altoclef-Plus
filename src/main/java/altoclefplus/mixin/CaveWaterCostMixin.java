package altoclefplus.mixin;

import altoclefplus.core.Hooks;
import altoclefplus.survival.CaveWater;
import baritone.pathing.movement.CalculationContext;
import baritone.pathing.movement.Moves;
import baritone.utils.pathing.MutableMoveResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Every move Baritone considers is costed here: make moves into cave water expensive (not forbidden). */
@Mixin(targets = "baritone.pathing.calc.AStarPathFinder", remap = false)
public abstract class CaveWaterCostMixin {
    @Redirect(method = "calculate0", require = 0, at = @At(value = "INVOKE",
            target = "Lbaritone/pathing/movement/Moves;apply(Lbaritone/pathing/movement/CalculationContext;IIILbaritone/utils/pathing/MutableMoveResult;)V"))
    private void altoclefplus$caveWaterCost(Moves move, CalculationContext ctx, int x, int y, int z, MutableMoveResult res) {
        move.apply(ctx, x, y, z, res);
        Hooks.hit("cave-water");
        if (res.cost < 1_000_000) {
            try {
                res.cost += CaveWater.extraCost(ctx, res.x, res.y, res.z);
            } catch (RuntimeException ignored) {
                // never break pathing over a cost tweak
            }
        }
    }
}
