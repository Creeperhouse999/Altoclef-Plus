package altoclefplus.core;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.Set;

/** Item ids that the running user task is collecting (from every resource task in its chain). */
public final class TaskTargets {
    private TaskTargets() {}

    public static Set<String> of(AltoClef mod) {
        Set<String> needed = new HashSet<>();
        for (Task t : mod.getUserTaskChain().getTasks()) {
            if (t instanceof ResourceTask rt) {
                for (ItemTarget target : rt.getItemTargets()) {
                    for (Item it : target.getMatches()) needed.add(Registry.ITEM.getId(it).getPath());
                }
            }
        }
        return needed;
    }
}
