package altoclefplus.mixin;

import altoclefplus.core.Hooks;
import altoclefplus.inventory.HotbarManager;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Wrong-tool fix. When the bot mines a block within reach, AltoClef holds left-click with Baritone stopped,
 * so nothing picks a tool, and Baritone's own picker prefers non-tools on ties (helmets, crafting tables).
 * Every block-break tick passes here; the method syncs the selected slot to the server right after.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ToolSelectMixin {
    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void altoclefplus$bestTool(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Hooks.hit("tool-fix");
        HotbarManager.beforeBreak(pos);
    }
}
