package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

/**
 * AltoClef's furnace task always reported "making a new furnace costs 9999999" (its cache field is never
 * null), so the bot walked to any furnace it had ever seen, however far. Only keep going back when that furnace
 * still holds our items; otherwise use AltoClef's own cobblestone-based cost (cheap when carrying a furnace).
 */
@Mixin(targets = "adris.altoclef.tasks.container.SmeltInFurnaceTask$DoSmeltInFurnaceTask", remap = false)
public abstract class FurnaceCostMixin {
    private static Field cacheField;
    private static Field materialField;
    private static Field outputField;

    @Inject(method = "getCostToMakeNew", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$realCost(AltoClef mod, CallbackInfoReturnable<Double> cir) {
        Hooks.hit("furnace-cost");
        if (!Plus.config().modules.inventory || !Plus.config().inventory.placeNearCraftingTable) return;
        try {
            if (furnaceHoldsOurItems()) return; // keep AltoClef's behaviour: go back for them
        } catch (ReflectiveOperationException | RuntimeException e) {
            return;
        }
        // Costs are in Baritone walking units: reuse any furnace within this many blocks instead of placing a
        // new one (a cost of 10 was cheaper than walking 3 blocks, so it placed furnace after furnace).
        double perBlock = 4.633;
        if (mod.getItemStorage().hasItem(Items.FURNACE)) {
            cir.setReturnValue(24 * perBlock);
            return;
        }
        int cobble = mod.getItemStorage().getItemCount(Items.COBBLESTONE);
        if (cobble >= 8) {
            cir.setReturnValue(40 * perBlock);
        } else {
            cir.setReturnValue((StorageHelper.miningRequirementMetInventory(mod, MiningRequirement.WOOD) ? 64 : 96) * perBlock);
        }
    }

    private boolean furnaceHoldsOurItems() throws ReflectiveOperationException {
        if (cacheField == null) {
            cacheField = getClass().getDeclaredField("_furnaceCache");
            cacheField.setAccessible(true);
        }
        Object cache = cacheField.get(this);
        if (cache == null) return false;
        if (materialField == null) {
            materialField = cache.getClass().getDeclaredField("materialSlot");
            outputField = cache.getClass().getDeclaredField("outputSlot");
            materialField.setAccessible(true);
            outputField.setAccessible(true);
        }
        return !((ItemStack) materialField.get(cache)).isEmpty() || !((ItemStack) outputField.get(cache)).isEmpty();
    }
}
