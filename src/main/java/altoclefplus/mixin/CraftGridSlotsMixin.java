package altoclefplus.mixin;

import adris.altoclef.BotBehaviour;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.slots.Slot;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

/**
 * Ender-eye crafting loop fix. AltoClef only counts a crafting-grid item if it sits in the exact slot of its own
 * recipe layout, but the recipe book places shapeless ingredients in vanilla order (pearl first). The bot then
 * thinks its powder and pearls vanished, switches tasks, empties the grid, and repeats forever. Count any
 * ingredient of the recipe in any grid slot instead.
 */
@Mixin(value = CraftInInventoryTask.class, remap = false)
public abstract class CraftGridSlotsMixin {
    @Shadow
    @Final
    private RecipeTarget _target;

    @Redirect(method = "onResourceStart", require = 0, at = @At(value = "INVOKE",
            target = "Ladris/altoclef/BotBehaviour;markSlotAsConversionSlot(Ladris/altoclef/util/slots/Slot;Ljava/util/function/Predicate;)V"))
    private void altoclefplus$anyIngredientCounts(BotBehaviour behaviour, Slot slot, Predicate<ItemStack> original) {
        Hooks.hit("craft-grid-fix");
        if (!Plus.config().modules.inventory) {
            behaviour.markSlotAsConversionSlot(slot, original);
            return;
        }
        ItemTarget[] ingredients = _target.getRecipe().getSlots();
        Predicate<ItemStack> anyIngredient = stack -> {
            for (ItemTarget t : ingredients) {
                if (t != null && !t.isEmpty() && t.matches(stack.getItem())) return true;
            }
            return false;
        };
        behaviour.markSlotAsConversionSlot(slot, original.or(anyIngredient));
    }
}
