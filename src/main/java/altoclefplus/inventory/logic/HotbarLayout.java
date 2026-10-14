package altoclefplus.inventory.logic;

import altoclefplus.core.logic.ItemInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fixed hotbar layout with hysteresis. If the wanted item is already on the hotbar, just select it; otherwise
 * move it into its pinned slot, or into a free / long-unchanged unpinned slot. This stops AltoClef swapping the
 * same two items back and forth between hand and inventory.
 */
public final class HotbarLayout {
    public enum Kind { SELECT, MOVE }

    public record Plan(Kind kind, int slot) {}

    private final Map<String, Integer> layout;
    private final Set<String> buildingBlocks;
    private final int hysteresisTicks;
    private final long[] lastMove = new long[9];

    public HotbarLayout(Map<String, Integer> layout, Set<String> buildingBlocks, int hysteresisTicks) {
        this.layout = layout;
        this.buildingBlocks = buildingBlocks;
        this.hysteresisTicks = hysteresisTicks;
        Arrays.fill(lastMove, Long.MIN_VALUE / 2);
    }

    public String category(String id) {
        String tool = ItemInfo.toolType(id);
        if (tool.equals("sword") || tool.equals("pickaxe") || tool.equals("axe") || tool.equals("shovel")) return tool;
        if (id.equals("water_bucket")) return "water_bucket";
        if (ItemInfo.isFood(id)) return "food";
        if (buildingBlocks.contains(id)) return "blocks";
        return "";
    }

    public int pinnedSlot(String id) {
        Integer slot = layout.get(category(id));
        return slot == null || slot < 0 || slot > 8 ? -1 : slot;
    }

    public void recordMove(int slot, long tick) {
        if (slot >= 0 && slot < 9) lastMove[slot] = tick;
    }

    public Plan plan(List<String> hotbar, String wanted, long tick) {
        int existing = hotbar.indexOf(wanted);
        if (existing >= 0) return new Plan(Kind.SELECT, existing);
        int pinned = pinnedSlot(wanted);
        if (pinned >= 0) return new Plan(Kind.MOVE, pinned);

        Set<Integer> pinnedSlots = new HashSet<>();
        for (Integer v : layout.values()) if (v != null && v >= 0 && v <= 8) pinnedSlots.add(v);
        for (int i = 0; i < 9; i++) {
            if (!pinnedSlots.contains(i) && hotbar.get(i).isEmpty()) return new Plan(Kind.MOVE, i);
        }
        for (int i = 0; i < 9; i++) {
            if (!pinnedSlots.contains(i) && tick - lastMove[i] >= hysteresisTicks) return new Plan(Kind.MOVE, i);
        }
        for (int i = 0; i < 9; i++) {
            if (!pinnedSlots.contains(i)) return new Plan(Kind.MOVE, i);
        }
        return new Plan(Kind.MOVE, 8);
    }
}
