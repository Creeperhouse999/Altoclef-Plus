package altoclefplus.mixin;

import adris.altoclef.tasksystem.TaskRunner;
import altoclefplus.core.Hooks;
import altoclefplus.core.UserScreens;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** While the player has their inventory open, no task runs (it carries on when the screen closes). */
@Mixin(value = TaskRunner.class, remap = false)
public abstract class UserScreenPauseMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$waitForPlayer(CallbackInfo ci) {
        Hooks.hit("user-screen-pause");
        if (UserScreens.pauseBot()) ci.cancel();
    }
}
