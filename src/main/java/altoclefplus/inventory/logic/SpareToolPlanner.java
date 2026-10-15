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
    private static final float PICK_REPLACE_BELOW = 0.15f;

    private SpareToolPlanner() {}

    public static Optional<String> next(Snapshot s) {
        int pick = ItemInfo.bestTier(s, "pickaxe");
        if (pick == ItemInfo.NONE) return Optional.empty();
        Optional<String> pickaxe = replacePickaxe(s);
        if (pickaxe.isPresent()) return pickaxe;
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

    /**
     * The only pickaxe is nearly broken: make a new one now, while the materials are still in the bag (same
     * tier if possible, else at least stone/wood). Without this, a broken pickaxe deep underground sent the bot
     * all the way back up for a tree.
     */
    private static Optional<String> replacePickaxe(Snapshot s) {
        String best = bestOfType(s, "pickaxe");
        if (best.isEmpty() || s.count(best) > 1 || s.durability.getOrDefault(best, 1f) >= PICK_REPLACE_BELOW) return Optional.empty();
        int tier = ItemInfo.tier(best);
        boolean sticks = s.count("stick") >= 2 || ItemInfo.countMatching(s, ItemInfo::isPlanks) + 4 * ItemInfo.countMatching(s, ItemInfo::isLog) >= 1;
        if (tier >= ItemInfo.IRON && s.count("iron_ingot") >= 3 && sticks) return Optional.of("iron_pickaxe");
        if (tier >= ItemInfo.DIAMOND && s.count("diamond") >= 3 && sticks) return Optional.of("diamond_pickaxe");
        if (canCraft(s, "stone_pickaxe")) return Optional.of("stone_pickaxe");
        if (canCraft(s, "wooden_pickaxe")) return Optional.of("wooden_pickaxe");
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
        boolean axe = id.endsWith("_axe") || id.endsWith("_pickaxe");
        int head = axe ? 3 : 1;
        if (id.startsWith("wooden_")) return planks >= head + stickPlanks;
        int stone = s.count("cobblestone") + s.count("cobbled_deepslate") + s.count("blackstone");
        return stone >= head && planks >= stickPlanks;
    }
}
