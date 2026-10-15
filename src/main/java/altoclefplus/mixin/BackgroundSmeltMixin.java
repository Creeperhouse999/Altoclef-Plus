package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.FurnaceSlot;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.inventory.logic.SmeltPlanner;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Background smelting: AltoClef stood at the furnace until everything was cooked. Once the furnace is filled
 * and there are 20+ seconds to wait, go mine a little coal (always useful as fuel) for at most that long, then
 * come back; AltoClef remembers the furnace and what is in it, so it walks back to the same one.
 */
@Mixin(targets = "adris.altoclef.tasks.container.SmeltInFurnaceTask$DoSmeltInFurnaceTask", remap = false)
public abstract class BackgroundSmeltMixin {
    @Unique
    private Task altoclefplus$sideTask;
    @Unique
    private long altoclefplus$sideUntilMs;
    @Unique
    private int altoclefplus$trips;
    @Unique
    private long altoclefplus$nextCheckMs;

    @Unique
    private static boolean altoclefplus$coalNearby(AltoClef mod) {
        var world = mod.getWorld();
        var c = mod.getPlayer().getBlockPos();
        for (var b : net.minecraft.util.math.BlockPos.iterate(c.add(-12, -6, -12), c.add(12, 6, 12))) {
            var block = world.getBlockState(b).getBlock();
            if (block == net.minecraft.block.Blocks.COAL_ORE || block == net.minecraft.block.Blocks.DEEPSLATE_COAL_ORE) return true;
        }
        return false;
    }

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$doSideJob(AltoClef mod, CallbackInfoReturnable<Task> cir) {
        if (altoclefplus$sideTask == null) return;
        if (System.currentTimeMillis() < altoclefplus$sideUntilMs && !altoclefplus$sideTask.isFinished(mod)) {
            cir.setReturnValue(altoclefplus$sideTask);
            return;
        }
        altoclefplus$sideTask = null;
        Plus.log().add("inventory", "Going back to the furnace");
    }

    @Inject(method = "containerSubTask", at = @At("RETURN"), require = 0)
    private void altoclefplus$maybeLeave(AltoClef mod, CallbackInfoReturnable<Task> cir) {
        Hooks.hit("background-smelt");
        // null here means "furnace filled, waiting".
        if (cir.getReturnValue() != null || !Plus.config().inventory.backgroundSmelting) return;
        if (altoclefplus$trips >= 2) return;
        // Only for coal ore right here: never an exploring trip far from the furnace. (Checked every 5 s at most.)
        long now = System.currentTimeMillis();
        if (now < altoclefplus$nextCheckMs) return;
        altoclefplus$nextCheckMs = now + 5_000;
        if (!altoclefplus$coalNearby(mod)) return;
        int coal = mod.getItemStorage().getItemCount(Items.COAL);
        int items = StorageHelper.getItemStackInSlot(FurnaceSlot.INPUT_SLOT_MATERIALS).getCount();
        int seconds = SmeltPlanner.sideJobSeconds(items, StorageHelper.getFurnaceCookPercent(), coal);
        if (seconds <= 0) return;
        seconds = Math.min(seconds, 60);
        Task coalTask = TaskCatalogue.getItemTask("coal", coal + 8);
        if (coalTask == null) return;
        altoclefplus$sideTask = coalTask;
        altoclefplus$trips++;
        altoclefplus$sideUntilMs = System.currentTimeMillis() + seconds * 1000L;
        Plus.log().add("inventory", "Smelting in the background (" + seconds + " s): getting coal meanwhile");
        StorageHelper.closeScreen();
    }
}
