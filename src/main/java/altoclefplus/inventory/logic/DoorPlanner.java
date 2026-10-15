package altoclefplus.inventory.logic;

import altoclefplus.core.logic.Snapshot;

import java.util.List;

/** Keep one wooden door for underwater air pockets, made only from wood already carried. */
public final class DoorPlanner {
    public static final List<String> WOODEN_DOORS = List.of("oak_door", "spruce_door", "birch_door", "jungle_door",
            "acacia_door", "dark_oak_door", "crimson_door", "warped_door");

    private DoorPlanner() {}

    public static boolean hasDoor(Snapshot s) {
        for (String d : WOODEN_DOORS) if (s.has(d)) return true;
        return false;
    }

    /** Craft a door now? Only with a pickaxe already made and enough wood in the bag (never a wood trip). */
    public static boolean shouldCraft(Snapshot s) {
        if (hasDoor(s)) return false;
        boolean pickaxe = false;
        int planks = 0;
        int logs = 0;
        for (var e : s.items.entrySet()) {
            String id = e.getKey();
            if (id.endsWith("_pickaxe")) pickaxe = true;
            if (id.endsWith("_planks")) planks += e.getValue();
            if (id.endsWith("_log") || id.endsWith("_stem")) logs += e.getValue();
        }
        return pickaxe && planks + logs * 4 >= 10;
    }
}
