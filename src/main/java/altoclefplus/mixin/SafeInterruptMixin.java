package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.SingleTaskChain;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskChain;
import altoclefplus.core.Hooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AltoClef crashes ("other is null") when the running chain is interrupted by no chain at all, which happens
 * when you leave a world while a fight or side job was the active chain. Interrupt it without the log line.
 */
@Mixin(value = SingleTaskChain.class, remap = false)
public abstract class SafeInterruptMixin {
    @Shadow
    protected Task _mainTask;
    @Shadow
    private boolean _interrupted;

    @Inject(method = "onInterrupt", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$nullSafe(AltoClef mod, TaskChain other, CallbackInfo ci) {
        Hooks.hit("safe-interrupt");
        if (other != null) return;
        _interrupted = true;
        if (_mainTask != null && _mainTask.isActive()) _mainTask.interrupt(mod, null);
        ci.cancel();
    }
}
