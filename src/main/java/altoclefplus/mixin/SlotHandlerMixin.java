package altoclefplus.mixin;

import adris.altoclef.control.SlotHandler;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import altoclefplus.core.Hooks;
import altoclefplus.inventory.HotbarManager;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hotbar thrash fix. AltoClef swapped every needed item into the selected slot, evicting whatever was there,
 * while Baritone swapped its pickaxe back into slot 0 every tick, so items bounced forever. Now: select an
 * item that's already on the hotbar; otherwise move it into its own slot.
 * Method descriptors use intermediary names because AltoClef's bytecode does (remap = false).
 */
@Mixin(value = SlotHandler.class, remap = false)
public abstract class SlotHandlerMixin {
    @Inject(method = "forceEquipSlot", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$equipSlot(Slot slot, CallbackInfo ci) {
        Hooks.hit("hotbar-fix");
        if (!altoclefplus$enabled() || Slot.isCursor(slot) || !(slot instanceof PlayerSlot)) return;
        if (HotbarManager.equipInventorySlot(slot.getInventorySlot())) ci.cancel();
    }

    @Inject(method = "forceEquipItem(Lnet/minecraft/class_1792;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$equipItem(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (!altoclefplus$enabled()) return;
        if (HotbarManager.equipItem(item)) cir.setReturnValue(true);
    }

    private static boolean altoclefplus$enabled() {
        return altoclefplus.Plus.mod() != null && altoclefplus.Plus.config().modules.inventory
                && altoclefplus.Plus.config().inventory.fixHotbar;
    }
}
