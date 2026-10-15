package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.FoodChain;
import altoclefplus.core.Hooks;
import altoclefplus.survival.DoorRules;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Door fix: eating holds right-click and pauses Baritone's clicks. While aiming at a door that blocked
 * Baritone from opening it, and the held use key toggled the door open/shut every few ticks. Don't start
 * eating while the crosshair is on a door, fence gate or trapdoor.
 */
@Mixin(value = FoodChain.class, remap = false)
public abstract class EatAtDoorMixin {
    @Inject(method = "startEat", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$notAtDoors(AltoClef mod, Item food, CallbackInfo ci) {
        Hooks.hit("door-eat");
        if (DoorRules.enabled() && DoorRules.aimingAtDoor()) ci.cancel();
    }
}
