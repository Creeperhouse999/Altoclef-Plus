package altoclefplus.mixin;

import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.survival.logic.SwimPolicy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** See {@link SwimPolicy}: in deep water let go of Baritone's jump and sprint, so the bot swims. */
@Mixin(targets = "baritone.utils.PlayerMovementInput", remap = false)
public abstract class SwimLikePlayerMixin extends Input {
    @Inject(method = {"tick(Z)V", "method_3129(Z)V"}, at = @At("TAIL"), require = 0)
    private void altoclefplus$swim(boolean slowDown, CallbackInfo ci) {
        Hooks.hit("swim-like-player");
        if (Plus.mod() == null || !Plus.config().survival.swimLikePlayer) return;
        var p = MinecraftClient.getInstance().player;
        if (p == null || p.world == null) return;
        BlockPos feet = p.getBlockPos();
        int depth = 0;
        while (depth < 3 && p.world.getFluidState(feet.down(depth)).isIn(net.minecraft.tag.FluidTags.WATER)) depth++;
        Direction facing = p.getHorizontalFacing();
        BlockPos ahead = feet.offset(facing);
        boolean blockAhead = !p.world.getBlockState(ahead).getCollisionShape(p.world, ahead).isEmpty()
                || !p.world.getBlockState(ahead.up()).getCollisionShape(p.world, ahead.up()).isEmpty();
        if (SwimPolicy.swim(p.isTouchingWater(), depth, p.getAir(), p.getMaxAir(), blockAhead, pressingForward)) {
            jumping = false;
            p.setSprinting(true);
        }
    }
}
