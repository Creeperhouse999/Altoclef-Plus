package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.trackers.BlockTracker;
import adris.altoclef.util.helpers.ItemHelper;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.survival.logic.BedChoice;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** See {@link BedChoice}: carrying a bed, only sleep in known beds close by; otherwise place our own. */
@Mixin(value = PlaceBedAndSetSpawnTask.class, remap = false)
public abstract class NearbyBedMixin {
    @Redirect(method = "onTick", require = 0, at = @At(value = "INVOKE",
            target = "Ladris/altoclef/trackers/BlockTracker;anyFound([Lnet/minecraft/class_2248;)Z"))
    private boolean altoclefplus$nearbyBedsOnly(BlockTracker tracker, Block[] blocks) {
        Hooks.hit("nearby-bed");
        AltoClef mod = Plus.mod();
        if (mod == null || mod.getPlayer() == null) return tracker.anyFound(blocks);
        boolean carrying = mod.getItemStorage().hasItem(ItemHelper.BED);
        return tracker.anyFound(pos -> BedChoice.useKnownBed(Math.sqrt(mod.getPlayer().squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)), carrying), blocks);
    }
}
