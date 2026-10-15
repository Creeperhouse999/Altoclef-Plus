package altoclefplus.mixin;

import adris.altoclef.control.PlayerExtraController;
import altoclefplus.core.Hooks;
import altoclefplus.survival.FairCombat;
import altoclefplus.survival.MeleeAssist;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every AltoClef melee hit goes through PlayerExtraController.attack, which only checks range: no hits
 * through walls or while facing away, and jump critical hits with a full charge bar.
 */
@Mixin(value = PlayerExtraController.class, remap = false)
public abstract class FairCombatMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$fairCombat(Entity entity, CallbackInfo ci) {
        Hooks.hit("fair-combat");
        if (!FairCombat.allow(entity) || !MeleeAssist.allowSwing(entity)) ci.cancel();
    }
}
