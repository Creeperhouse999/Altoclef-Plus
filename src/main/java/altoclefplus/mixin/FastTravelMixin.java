package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.FastTravelTask;
import adris.altoclef.tasksystem.Task;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.survival.logic.StrongholdTravel;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Nether fast-travel fixes:
 * - decide walk vs. Nether once per destination (AltoClef re-decided every time the task was recreated,
 *   bouncing between dimensions);
 * - collect 10 obsidian and flint and steel in the Overworld before entering, instead of lava-casting in the
 *   Nether where water evaporates;
 * - exact Nether coordinates (x / 8 was off by a block for negative coordinates).
 */
@Mixin(value = FastTravelTask.class, remap = false)
public abstract class FastTravelMixin {
    private static final Map<Long, Boolean> DECISIONS = new HashMap<>();

    @Shadow
    @Final
    private BlockPos _target;

    @Shadow
    private boolean _forceOverworldWalking;

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$decideOnce(AltoClef mod, CallbackInfoReturnable<Task> cir) {
        Hooks.hit("fast-travel");
        if (!enabled() || mod.getPlayer() == null || mod.getWorld().getRegistryKey() != World.OVERWORLD) return;
        double dist = Math.hypot(mod.getPlayer().getX() - _target.getX(), mod.getPlayer().getZ() - _target.getZ());
        boolean allowed = mod.getModSettings().getNetherFastTravelWalkingRange() < 1_000_000;
        if (DECISIONS.size() > 64) DECISIONS.clear();
        boolean nether = DECISIONS.computeIfAbsent(_target.asLong(),
                k -> StrongholdTravel.useNether(dist, Plus.config().survival.netherTravelMinDistance, allowed));
        if (!nether || !allowed) {
            _forceOverworldWalking = true;
            return;
        }
        if (_forceOverworldWalking) return; // close enough: AltoClef walks the rest
        if (mod.getItemStorage().getItemCount(Items.OBSIDIAN) < 10) {
            cir.setReturnValue(TaskCatalogue.getItemTask(Items.OBSIDIAN, 10));
        } else if (!mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)) {
            cir.setReturnValue(TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1));
        }
    }

    @ModifyVariable(method = "onTick", at = @At("STORE"), ordinal = 0, require = 0)
    private BlockPos altoclefplus$exactNetherTarget(BlockPos value) {
        if (!enabled()) return value;
        return new BlockPos(StrongholdTravel.netherCoord(_target.getX()), value.getY(), StrongholdTravel.netherCoord(_target.getZ()));
    }

    private static boolean enabled() {
        return Plus.mod() != null && Plus.config().modules.survival && Plus.config().survival.preciseNetherTravel;
    }
}
