package altoclefplus.mixin;

import adris.altoclef.control.SlotHandler;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import altoclefplus.core.Hooks;
import altoclefplus.inventory.VisibleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Visible inventory: a bot click in its own inventory opens the inventory screen first. */
@Mixin(value = SlotHandler.class, remap = false)
public abstract class VisibleSlotClickMixin {
    @Inject(method = "clickSlot", at = @At("HEAD"), require = 0)
    private void altoclefplus$showInventory(Slot slot, int mouseButton,
                                            net.minecraft.screen.slot.SlotActionType type, CallbackInfo ci) {
        Hooks.hit("visible-inventory");
        VisibleInventory.beforeClick(slot instanceof PlayerSlot);
    }
}
