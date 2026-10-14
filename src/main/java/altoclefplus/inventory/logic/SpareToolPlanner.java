package altoclefplus.inventory.logic;

import altoclefplus.core.logic.ItemInfo;
import altoclefplus.core.logic.Snapshot;

import java.util.Map;
import java.util.Optional;

/**
 * Keeps a cheap axe and shovel (wood or stone, matching the pickaxe) so the bot stops chopping trees and
 * digging dirt by hand, and replaces them before they break. Only plans what can be crafted from materials
 * already carried, so it never sends the bot off on a gathering trip.
 */
public final class SpareToolPlanner {
    private static final float REPLACE_BELOW = 0.10f;

    private SpareToolPlanner() {}

    public static Optional<String> next(Snapshot s) {
        int pick = ItemInfo.bestTier(s, "pickaxe");
        if (pick == ItemInfo.NONE) return Optional.empty();
        String prefix = pick >= ItemInfo.STONE ? "stone" : "wooden";
        int target = pick >= ItemInfo.STONE ? ItemInfo.STONE : ItemInfo.WOOD;

        for (String type : new String[]{"axe", "shovel"}) {
            String want = null;
            int owned = ItemInfo.bestTier(s, type);
            if (owned < target) {
                want = prefix + "_" + type;
            } else {
                String bestId = bestOfType(s, type);
                float left = s.durability.getOrDefault(bestId, 1f);
                if (left < REPLACE_BELOW && s.count(bestId) == 1 && ItemInfo.tier(bestId) <= ItemInfo.STONE) want = bestId;
            }
            if (want != null && canCraft(s, want)) return Optional.of(want);
        }
        return Optional.empty();
    }

    private static String bestOfType(Snapshot s, String type) {
        String best = "";
        for (Map.Entry<String, Integer> e : s.items.entrySet()) {
            if (type.equals(ItemInfo.toolType(e.getKey())) && ItemInfo.tier(e.getKey()) >= ItemInfo.tier(best)) {
                best = e.getKey();
            }
        }
        return best;
    }

    /** Planks-equivalent: planks + 4 per log; sticks reduce the planks needed. */
    static boolean canCraft(Snapshot s, String id) {
        int planks = ItemInfo.countMatching(s, ItemInfo::isPlanks) + 4 * ItemInfo.countMatching(s, ItemInfo::isLog);
        int stickPlanks = s.count("stick") >= 2 ? 0 : 1;
        boolean axe = id.endsWith("_axe");
        int head = axe ? 3 : 1;
        if (id.startsWith("wooden_")) return planks >= head + stickPlanks;
        int stone = s.count("cobblestone") + s.count("cobbled_deepslate") + s.count("blackstone");
        return stone >= head && planks >= stickPlanks;
    }
}
