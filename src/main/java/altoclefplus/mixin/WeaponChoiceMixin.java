package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.entity.AbstractKillEntityTask;
import altoclefplus.core.Hooks;
import altoclefplus.survival.MeleeAssist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AltoClef picks weapons from a fixed list (any sword beats any axe, netherite missing). Choose by situation:
 * axe vs shields and for finishing blows, sword for groups, otherwise the most damage per second.
 */
@Mixin(value = AbstractKillEntityTask.class, remap = false)
public abstract class WeaponChoiceMixin {
    @Inject(method = "equipWeapon", at = @At("HEAD"), cancellable = true, require = 0)
    private static void altoclefplus$chooseWeapon(AltoClef mod, CallbackInfo ci) {
        Hooks.hit("weapon-choice");
        if (MeleeAssist.equipWeapon()) ci.cancel();
    }
}
