package altoclefplus.mixin;

import adris.altoclef.chains.PlayerInteractionFixChain;
import altoclefplus.core.Hooks;
import altoclefplus.ui.PlusScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** AltoClef closes any open screen when the bot turns; keep our panel, your inventory and our visible inventory open. */
@Mixin(value = PlayerInteractionFixChain.class, remap = false)
public abstract class InteractionFixChainMixin {
    @Inject(method = "shouldCloseOpenScreen", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$keepPanel(CallbackInfoReturnable<Boolean> cir) {
        Hooks.hit("keep-panel-open");
        if (MinecraftClient.getInstance().currentScreen instanceof PlusScreen
                || altoclefplus.core.UserScreens.pauseBot()
                || altoclefplus.inventory.VisibleInventory.isOurs()) cir.setReturnValue(false);
    }
}
