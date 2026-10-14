package altoclefplus.mixin;

import adris.altoclef.tasks.resources.CollectObsidianTask;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * AltoClef's obsidian task had its dimension check backwards: it wandered in the Overworld and tried to
 * lava-cast obsidian in the Nether, where water evaporates (the "cross with lava in one corner"). Flip it.
 * Target uses intermediary names (DimensionType.isUltrawarm) because AltoClef's bytecode does.
 */
@Mixin(value = CollectObsidianTask.class, remap = false)
public abstract class ObsidianCastMixin {
    @Redirect(method = "onResourceTick", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2874;method_27999()Z"))
    private boolean altoclefplus$castOnlyWhereWaterWorks(DimensionType dimension) {
        Hooks.hit("obsidian-cast");
        boolean ultrawarm = dimension.isUltrawarm();
        boolean fix = Plus.mod() != null && Plus.config().modules.survival && Plus.config().survival.preciseNetherTravel;
        return fix ? !ultrawarm : ultrawarm;
    }
}
