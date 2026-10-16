package altoclefplus.mixin;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.PickupFromContainerTask;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.ItemTarget;
import altoclefplus.Plus;
import altoclefplus.build.logic.ChestRules;
import altoclefplus.core.Hooks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * Chest rules for AltoClef's gathering: take from chests only if allowed, skip chests in safe zones if that is
 * switched off, never pick a chest that holds no more than the "leave at least" amount, and cap what is taken
 * so that amount stays behind (e.g. a stocked chest always keeps one stack).
 */
@Mixin(value = ResourceTask.class, remap = false)
public abstract class ChestTakeRulesMixin {
    @Redirect(method = "onTick", require = 0, at = @At(value = "INVOKE",
            target = "Ladris/altoclef/trackers/storage/ItemStorageTracker;getContainersWithItem([Lnet/minecraft/class_1792;)Ljava/util/List;"))
    private List<ContainerCache> altoclefplus$allowedChests(ItemStorageTracker tracker, Item[] items) {
        Hooks.hit("chest-rules");
        List<ContainerCache> all = tracker.getContainersWithItem(items);
        var b = Plus.config().basics;
        if (!b.takeFromChests) return new ArrayList<>();
        List<ContainerCache> out = new ArrayList<>();
        for (ContainerCache c : all) {
            if (!ChestRules.appliesTo(c.getContainerType().name())) {
                out.add(c);
                continue;
            }
            if (c.getItemCount(items) <= b.chestLeaveAtLeast) continue;
            if (!b.takeFromZoneChests && inLootProtectedZone(c.getBlockPos())) continue;
            out.add(c);
        }
        return out;
    }

    @Redirect(method = "onTick", require = 0, at = @At(value = "NEW",
            target = "Ladris/altoclef/tasks/container/PickupFromContainerTask;<init>(Lnet/minecraft/class_2338;[Ladris/altoclef/util/ItemTarget;)V"))
    private PickupFromContainerTask altoclefplus$cappedPickup(BlockPos pos, ItemTarget[] targets) {
        AltoClef mod = Plus.mod();
        int leave = Plus.config().basics.chestLeaveAtLeast;
        if (mod == null || leave <= 0) return new PickupFromContainerTask(pos, targets);
        var cache = mod.getItemStorage().getContainerAtPosition(pos);
        // Furnace output (the bot's own smelting) is always taken in full.
        if (cache.isPresent() && !ChestRules.appliesTo(cache.get().getContainerType().name())) return new PickupFromContainerTask(pos, targets);
        ItemTarget[] capped = new ItemTarget[targets.length];
        for (int i = 0; i < targets.length; i++) {
            ItemTarget t = targets[i];
            int carried = mod.getItemStorage().getItemCount(t.getMatches());
            int inChest = cache.map(c -> c.getItemCount(t.getMatches())).orElse(0);
            capped[i] = new ItemTarget(t, ChestRules.cappedTarget(carried, t.getTargetCount(), inChest, leave));
        }
        return new PickupFromContainerTask(pos, capped);
    }

    private static boolean inLootProtectedZone(BlockPos p) {
        return altoclefplus.zones.Zones.current().forbids(altoclefplus.zones.logic.ZoneSet.Action.LOOT, p.getX(), p.getY(), p.getZ());
    }
}
