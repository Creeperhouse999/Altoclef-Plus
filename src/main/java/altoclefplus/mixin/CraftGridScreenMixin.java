package altoclefplus.mixin;

import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import altoclefplus.core.Hooks;
import altoclefplus.inventory.logic.CraftGrid;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** See {@link CraftGrid}: the 2x2 grid slots are not "open" while a furnace or other container is. */
@Mixin(value = Slot.class, remap = false)
public abstract class CraftGridScreenMixin {
    @Inject(method = "isScreenOpen", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$gridOnlyOnOwnScreen(CallbackInfoReturnable<Boolean> cir) {
        Hooks.hit("craft-grid-screen");
        if (!((Object) this instanceof PlayerSlot)) return;
        boolean grid = false;
        for (PlayerSlot s : PlayerSlot.CRAFT_INPUT_SLOTS) {
            if (s.equals(this)) {
                grid = true;
                break;
            }
        }
        var screen = MinecraftClient.getInstance().currentScreen;
        if (!CraftGrid.visible(grid, screen == null || screen instanceof InventoryScreen)) cir.setReturnValue(false);
    }
}
