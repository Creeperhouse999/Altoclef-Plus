package altoclefplus.mixin;

import adris.altoclef.ui.CommandStatusOverlay;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides AltoClef's own task-chain text overlay; the Plus HUD replaces it. */
@Mixin(value = CommandStatusOverlay.class, remap = false)
public abstract class CommandStatusOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$hide(CallbackInfo ci) {
        Hooks.hit("altoclef-overlay");
        if (Plus.config().modules.ui && Plus.config().ui.hideAltoClefOverlay) ci.cancel();
    }
}
