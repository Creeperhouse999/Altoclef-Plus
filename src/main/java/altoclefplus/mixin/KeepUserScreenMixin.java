package altoclefplus.mixin;

import adris.altoclef.util.helpers.StorageHelper;
import altoclefplus.core.Hooks;
import altoclefplus.core.UserScreens;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** AltoClef closes "any screen" at many task starts; never close the inventory the player opened. */
@Mixin(value = StorageHelper.class, remap = false)
public abstract class KeepUserScreenMixin {
    @Inject(method = "closeScreen", at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$keepUserScreen(CallbackInfo ci) {
        Hooks.hit("keep-user-screen");
        if (UserScreens.pauseBot()) ci.cancel();
    }
}
