package altoclefplus.inventory.logic;

import altoclefplus.core.logic.ItemInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Conservative junk rules: only items on the junk list, plus building blocks above their caps, may ever be
 * dropped — and only when the inventory is nearly full. Protected items, items the current task needs,
 * tools, weapons and armor are never dropped. (Enchanted/renamed stacks are filtered at the slot level.)
 */
public final class JunkClassifier {
    public record Drop(String id, int count) {}

    /**
     * Picked up while mining and never used by @gamer or crafting it does. Always junk unless protected or
     * needed by the current task, on top of the configurable list.
     */
    public static final Set<String> EXTRA_JUNK = Set.of(
            "calcite", "smooth_basalt", "amethyst_block", "amethyst_shard", "budding_amethyst", "raw_copper",
            "copper_ingot", "pointed_dripstone", "dripstone_block", "moss_block", "moss_carpet", "clay_ball",
            "glow_ink_sac", "glow_berries", "kelp", "lily_pad", "dead_bush", "grass", "fern", "tall_grass", "large_fern");

    private final Set<String> protectedIds;
    private final Set<String> junkIds;
    private final Set<String> buildingIds;
    private final int capPerType;
    private final int capTotal;
    private final int freeSlotThreshold;

    public JunkClassifier(Set<String> protectedIds, Set<String> junkIds, Set<String> buildingIds,
                          int capPerType, int capTotal, int freeSlotThreshold) {
        this.protectedIds = protectedIds;
        this.junkIds = junkIds;
        this.buildingIds = buildingIds;
        this.capPerType = capPerType;
        this.capTotal = capTotal;
        this.freeSlotThreshold = freeSlotThreshold;
    }

    public List<Drop> toDrop(Map<String, Integer> items, Set<String> neededByTask, int freeSlots) {
        List<Drop> drops = new ArrayList<>();
        if (freeSlots > freeSlotThreshold) return drops;

        Map<String, Integer> sorted = new TreeMap<>(items);
        for (Map.Entry<String, Integer> e : sorted.entrySet()) {
            String id = e.getKey();
            if (!(junkIds.contains(id) || EXTRA_JUNK.contains(id)) || !droppable(id, neededByTask)) continue;
            if (id.equals("rotten_flesh") && otherFoodUnits(items) == 0) continue;
            drops.add(new Drop(id, e.getValue()));
        }

        Map<String, Integer> building = new HashMap<>();
        for (Map.Entry<String, Integer> e : sorted.entrySet()) {
            String id = e.getKey();
            if (!buildingIds.contains(id) || junkIds.contains(id) || !droppable(id, neededByTask)) continue;
            int count = e.getValue();
            if (count > capPerType) {
                drops.add(new Drop(id, count - capPerType));
                count = capPerType;
            }
            building.put(id, count);
        }
        int total = building.values().stream().mapToInt(Integer::intValue).sum();
        while (total > capTotal) {
            Map.Entry<String, Integer> largest = building.entrySet().stream()
                    .max(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                            .thenComparing(Map.Entry::getKey, Comparator.reverseOrder()))
                    .orElseThrow();
            int cut = Math.min(largest.getValue(), total - capTotal);
            merge(drops, largest.getKey(), cut);
            largest.setValue(largest.getValue() - cut);
            total -= cut;
        }
        return drops;
    }

    /**
     * Tools beaten by a better one of the same kind (a wooden pickaxe once a stone one is carried). Only once
     * the best is stone or better; spares of the best tier, needed and protected items are kept.
     */
    public static List<Drop> outdatedTools(Map<String, Integer> items, Set<String> neededByTask, Set<String> protectedIds) {
        Map<String, Integer> best = new HashMap<>();
        for (String id : items.keySet()) {
            String type = ItemInfo.toolType(id);
            if (!type.isEmpty()) best.merge(type, ItemInfo.tier(id), Math::max);
        }
        List<Drop> drops = new ArrayList<>();
        for (Map.Entry<String, Integer> e : new TreeMap<>(items).entrySet()) {
            String id = e.getKey();
            String type = ItemInfo.toolType(id);
            if (type.isEmpty() || neededByTask.contains(id) || protectedIds.contains(id)) continue;
            int top = best.get(type);
            if (top >= ItemInfo.STONE && ItemInfo.tier(id) < top) drops.add(new Drop(id, e.getValue()));
            // One in use and one spare is plenty.
            else if (ItemInfo.tier(id) == top && e.getValue() > 2) drops.add(new Drop(id, e.getValue() - 2));
        }
        return drops;
    }

    /**
     * Carried armor no better than what is worn in its slot (iron chestplate under a diamond one). Golden boots
     * are kept: they are for the Nether.
     */
    public static List<Drop> outdatedArmor(Map<String, Integer> items, Map<String, String> worn, Set<String> neededByTask,
                                           Set<String> protectedIds) {
        List<Drop> drops = new ArrayList<>();
        for (Map.Entry<String, Integer> e : new TreeMap<>(items).entrySet()) {
            String id = e.getKey();
            String slot = ItemInfo.armorSlot(id);
            if (slot.isEmpty() || id.equals("golden_boots") || neededByTask.contains(id) || protectedIds.contains(id)) continue;
            String on = worn.getOrDefault(slot, "");
            if (!on.isEmpty() && ItemInfo.armorPoints(id) <= ItemInfo.armorPoints(on)) drops.add(new Drop(id, e.getValue()));
        }
        return drops;
    }

    private boolean droppable(String id, Set<String> needed) {
        return !protectedIds.contains(id) && !needed.contains(id)
                && ItemInfo.toolType(id).isEmpty() && ItemInfo.armorSlot(id).isEmpty() && !id.equals("shield");
    }

    private static int otherFoodUnits(Map<String, Integer> items) {
        int units = 0;
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            if (!e.getKey().equals("rotten_flesh")) units += ItemInfo.foodUnits(e.getKey()) * e.getValue();
        }
        return units;
    }

    private static void merge(List<Drop> drops, String id, int count) {
        for (int i = 0; i < drops.size(); i++) {
            if (drops.get(i).id().equals(id)) {
                drops.set(i, new Drop(id, drops.get(i).count() + count));
                return;
            }
        }
        drops.add(new Drop(id, count));
    }
}
