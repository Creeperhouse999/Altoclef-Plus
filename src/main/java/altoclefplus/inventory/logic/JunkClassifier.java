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
            if (!junkIds.contains(id) || !droppable(id, neededByTask)) continue;
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
