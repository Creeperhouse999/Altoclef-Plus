package altoclefplus.survival;

import altoclefplus.Plus;
import altoclefplus.survival.logic.WaterSafety;
import baritone.pathing.movement.CalculationContext;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;

/**
 * Path cost for water in caves (called from Baritone's pathing thread). Cave water is where the bot got stuck
 * and drowned; lakes and oceans under open sky keep their normal cost so boats and swimming still work.
 */
public final class CaveWater {
    private static final int ROOF_SCAN = 32;

    private CaveWater() {}

    public static double extraCost(CalculationContext ctx, int x, int y, int z) {
        var cfg = Plus.config();
        if (cfg == null || !cfg.modules.survival || !cfg.survival.avoidCaveWater) return 0;
        if (!isWater(ctx.get(x, y, z))) return 0;
        boolean head = isWater(ctx.get(x, y + 1, z));
        return WaterSafety.extraCost(true, head, underground(ctx, x, y, z), cfg.survival.caveWaterPenalty);
    }

    /** A solid (non-fluid) block somewhere above: a cave ceiling, not open sky. */
    private static boolean underground(CalculationContext ctx, int x, int y, int z) {
        for (int dy = 2; dy <= ROOF_SCAN; dy++) {
            BlockState b = ctx.get(x, y + dy, z);
            if (b.isAir() || !b.getFluidState().isEmpty()) continue;
            if (b.getMaterial().blocksMovement()) return true;
        }
        return false;
    }

    private static boolean isWater(BlockState s) {
        return s.getFluidState().getFluid() == Fluids.WATER || s.getFluidState().getFluid() == Fluids.FLOWING_WATER;
    }
}
