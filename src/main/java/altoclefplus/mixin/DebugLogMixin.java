package altoclefplus.mixin;

import adris.altoclef.Debug;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sends AltoClef's chat logging to the AltoClef Plus log instead of chat. */
@Mixin(value = Debug.class, remap = false)
public abstract class DebugLogMixin {
    @Inject(method = "logMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$message(String message, boolean prefix, CallbackInfo ci) {
        Hooks.hit("altoclef-log");
        if (redirect()) {
            Plus.log().add("altoclef", message);
            ci.cancel();
        }
    }

    @Inject(method = "logWarning(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$warning(String message, CallbackInfo ci) {
        if (redirect()) {
            Plus.log().add("altoclef", "WARN " + message);
            ci.cancel();
        }
    }

    @Inject(method = "logError(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$error(String message, CallbackInfo ci) {
        if (redirect()) {
            Plus.log().add("altoclef", "ERROR " + message);
            System.err.println("ALTO CLEF ERROR: " + message);
            ci.cancel();
        }
    }

    private static boolean redirect() {
        return Plus.mod() != null && Plus.config().modules.ui && Plus.config().ui.redirectChat;
    }
}
