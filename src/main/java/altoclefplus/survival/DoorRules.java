package altoclefplus.survival;

import altoclefplus.Plus;
import baritone.pathing.movement.CalculationContext;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;

/** Shared checks for the door fixes (used by the door mixins). */
public final class DoorRules {
    private DoorRules() {}

    public static boolean enabled() {
        return Plus.mod() != null && Plus.config().modules.survival && Plus.config().survival.fixDoors;
    }

    /** Any of the cells holds a wooden door or fence gate (things only the straight move opens). */
    public static boolean touchesDoor(CalculationContext ctx, List<int[]> cells) {
        for (int[] c : cells) {
            Block b = ctx.get(c[0], c[1], c[2]).getBlock();
            if (b instanceof DoorBlock || b instanceof FenceGateBlock) return true;
        }
        return false;
    }

    /** The crosshair is on a door, fence gate or trapdoor (right-click would toggle it). */
    public static boolean aimingAtDoor() {
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult hit = client.crosshairTarget;
        if (client.world == null || !(hit instanceof BlockHitResult b) || hit.getType() != HitResult.Type.BLOCK) return false;
        Block block = client.world.getBlockState(b.getBlockPos()).getBlock();
        return block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof TrapdoorBlock;
    }
}
