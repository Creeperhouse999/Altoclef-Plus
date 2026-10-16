package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftGenericManuallyTask;
import adris.altoclef.tasks.CraftGenericWithRecipeBooksTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.RecipeTarget;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.inventory.logic.RecipeBookFallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** See {@link RecipeBookFallback}: when the recipe book click does nothing, craft by hand. */
@Mixin(value = CraftGenericWithRecipeBooksTask.class, remap = false)
public abstract class RecipeBookFallbackMixin {
    @Shadow
    @Final
    private RecipeTarget _target;

    @Unique
    private final RecipeBookFallback altoclefplus$fallback = new RecipeBookFallback();
    @Unique
    private Task altoclefplus$manual;

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$keepManual(AltoClef mod, CallbackInfoReturnable<Task> cir) {
        Hooks.hit("recipe-book-fallback");
        if (altoclefplus$manual != null) cir.setReturnValue(altoclefplus$manual);
    }

    @Inject(method = "onTick", at = @At("RETURN"), cancellable = true, require = 0)
    private void altoclefplus$fallBack(AltoClef mod, CallbackInfoReturnable<Task> cir) {
        if (altoclefplus$manual != null) return;
        // The only "nothing to do" return is "Waiting for recipe book click...".
        if (cir.getReturnValue() != null) {
            altoclefplus$fallback.progressed();
            return;
        }
        if (altoclefplus$fallback.waiting(System.currentTimeMillis())) {
            altoclefplus$manual = new CraftGenericManuallyTask(_target);
            Plus.log().add("inventory", "Recipe book did nothing: crafting " + _target.getOutputItem() + " by hand");
            cir.setReturnValue(altoclefplus$manual);
        }
    }
}
