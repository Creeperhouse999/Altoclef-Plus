package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import adris.altoclef.trackers.BlockTracker;
import altoclefplus.Plus;
import adris.altoclef.tasksystem.Task;
import altoclefplus.core.Hooks;
import altoclefplus.survival.EndermanBoatHuntTask;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/** Fixes inside @gamer (BeatMinecraft2Task). */
@Mixin(value = BeatMinecraft2Task.class, remap = false)
public abstract class BeatMinecraftFixesMixin {
    /**
     * "Pick up crafting table while we're at it" had no distance limit: any table ever seen (even one in a
     * village 300 blocks away) made the bot tunnel back to it. Only count tables within the configured range.
     * The descriptor uses intermediary names because AltoClef's bytecode does.
     */
    @Redirect(method = "onTick", require = 0, at = @At(value = "INVOKE",
            target = "Ladris/altoclef/trackers/BlockTracker;anyFound(Ljava/util/function/Predicate;[Lnet/minecraft/class_2248;)Z"))
    private boolean altoclefplus$nearbyTablesOnly(BlockTracker tracker, Predicate<BlockPos> test, Block[] blocks) {
        Hooks.hit("table-distance");
        if (!Plus.config().modules.inventory || !Plus.config().inventory.placeNearCraftingTable || !contains(blocks, Blocks.CRAFTING_TABLE)) {
            return tracker.anyFound(test, blocks);
        }
        var player = MinecraftClient.getInstance().player;
        if (player == null) return tracker.anyFound(test, blocks);
        double max = Plus.config().inventory.craftingTableMaxDistance;
        return tracker.anyFound(test.and(pos -> pos.isWithinDistance(player.getPos(), max)), blocks);
    }

    /** Blaze powder isn't protected by AltoClef, so it can get thrown away mid eye-crafting. */
    @Inject(method = "onStart", at = @At("RETURN"), require = 0)
    private void altoclefplus$protectPowder(AltoClef mod, CallbackInfo ci) {
        if (Plus.config().modules.inventory) mod.getBehaviour().addProtectedItems(Items.BLAZE_POWDER);
    }

    /**
     * Pearls: replace "wander until an enderman shows up, then chase it" with the boat-trap hunt in a warped
     * forest. Bartering (if chosen in AltoClef's @gamer config) is left alone.
     */
    @Inject(method = "getEnderPearlTask", at = @At("HEAD"), cancellable = true, require = 0)
    private void altoclefplus$boatTrapPearls(AltoClef mod, int count, CallbackInfoReturnable<Task> cir) {
        Hooks.hit("enderman-hunt");
        if (!Plus.config().modules.survival || !Plus.config().survival.endermanBoatHunt) return;
        if (BeatMinecraft2Task.getConfig().barterPearlsInsteadOfEndermanHunt) return;
        cir.setReturnValue(new EndermanBoatHuntTask(count));
    }

    private static boolean contains(Block[] blocks, Block wanted) {
        for (Block b : blocks) if (b == wanted) return true;
        return false;
    }
}
