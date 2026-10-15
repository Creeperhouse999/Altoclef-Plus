package altoclefplus.mixin;

import altoclefplus.core.Hooks;
import altoclefplus.zones.Zones;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hard guard for safe zones and break rules: while the bot runs a task, its own clicks can't break or place
 * blocks or open containers where that's forbidden - whatever AltoClef or Baritone decided. Your own clicks
 * (bot not running) are never blocked.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ZoneGuardMixin {
    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void altoclefplus$guardAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Hooks.hit("zone-guard");
        if (Zones.botActive() && !Zones.mayBreak(pos)) cir.setReturnValue(false);
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void altoclefplus$guardBreak(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (Zones.botActive() && !Zones.mayBreak(pos)) cir.setReturnValue(false);
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void altoclefplus$guardUse(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hit,
                                       CallbackInfoReturnable<ActionResult> cir) {
        if (!Zones.botActive()) return;
        BlockPos pos = hit.getBlockPos();
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Inventory && !Zones.mayLoot(pos)) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if (player.getStackInHand(hand).getItem() instanceof BlockItem) {
            BlockState state = world.getBlockState(pos);
            BlockPos placeAt = state.getMaterial().isReplaceable() ? pos : pos.offset(hit.getSide());
            if (!Zones.mayPlace(placeAt)) cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
