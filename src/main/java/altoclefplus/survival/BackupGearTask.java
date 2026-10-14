package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * Makes a spare iron kit (sword, pickaxe, full armor) and leaves it in a chest, so a death in the Nether or
 * the End doesn't mean starting from nothing. AltoClef counts worn armor, so "2" = what's worn + 1 spare.
 */
public final class BackupGearTask extends Task {
    private static final Item[] GEAR = {Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_HELMET,
            Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS};

    private Task storeTask;
    private boolean storing;

    @Override
    protected void onStart(AltoClef mod) {
        storing = false;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (!storing) {
            ItemTarget[] two = new ItemTarget[GEAR.length];
            boolean haveAll = true;
            for (int i = 0; i < GEAR.length; i++) {
                two[i] = new ItemTarget(GEAR[i], 2);
                if (mod.getItemStorage().getItemCount(GEAR[i]) < 2) haveAll = false;
            }
            if (!haveAll) {
                setDebugState("Making a backup set of gear");
                return TaskCatalogue.getSquashedItemTask(two);
            }
            storing = true;
            ItemTarget[] one = new ItemTarget[GEAR.length];
            for (int i = 0; i < GEAR.length; i++) one[i] = new ItemTarget(GEAR[i], 1);
            storeTask = new StoreInAnyContainerTask(false, one);
        }
        setDebugState("Putting backup gear in a chest");
        return storeTask;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return storing && storeTask != null && storeTask.isFinished(mod);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BackupGearTask;
    }

    @Override
    protected String toDebugString() {
        return "Backup gear chest";
    }
}
