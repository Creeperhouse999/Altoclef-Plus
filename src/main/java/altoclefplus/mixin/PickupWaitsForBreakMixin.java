package altoclefplus.mixin;

import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.trackers.EntityTracker;
import adris.altoclef.util.ItemTarget;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.core.logic.Snapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * AltoClef switched to "pick up the drop" the moment the previous block's item appeared, even while it was
 * half-way through breaking the next block — so the break restarted again and again. While a block is being
 * broken, the drops wait (our pickup step collects them right after).
 */
@Mixin(value = ResourceTask.class, remap = false)
public abstract class PickupWaitsForBreakMixin {
    @Shadow
    @Final
    private adris.altoclef.tasks.movement.PickupDroppedItemTask _pickupTask;

    @Redirect(method = "onTick", require = 0, at = @At(value = "INVOKE",
            target = "Ladris/altoclef/trackers/EntityTracker;itemDropped([Ladris/altoclef/util/ItemTarget;)Z"))
    private boolean altoclefplus$notWhileBreaking(EntityTracker tracker, ItemTarget[] targets) {
        Hooks.hit("pickup-waits");
        boolean dropped = tracker.itemDropped(targets);
        if (!dropped || Plus.mod() == null || !Plus.config().inventory.pickupDrops) return dropped;
        // A pickup already under way is finished, even if it breaks blocks on the way to the item.
        if (_pickupTask != null && _pickupTask.isActive()) return true;
        return Plus.snapshot().breakingPos == Snapshot.NO_POS;
    }
}
