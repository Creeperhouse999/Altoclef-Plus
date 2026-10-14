package altoclefplus.inventory.logic;

import altoclefplus.core.logic.ItemInfo;
import altoclefplus.core.logic.Snapshot;

import java.util.List;
import java.util.Optional;

/**
 * Piece-by-piece armor: craft the best affordable upgrade as soon as materials allow, after reserving what
 * tools need. Also decides which carried piece to wear (golden boots stay on in the Nether for piglins).
 */
public final class ArmorPlanner {
    private static final String[] ORDER = {"chest", "legs", "head", "feet"};

    private ArmorPlanner() {}

    public static int cost(String slot) {
        return switch (slot) {
            case "chest" -> 8;
            case "legs" -> 7;
            case "head" -> 5;
            default -> 4;
        };
    }

    public static String pieceName(String slot) {
        return switch (slot) {
            case "chest" -> "chestplate";
            case "legs" -> "leggings";
            case "head" -> "helmet";
            default -> "boots";
        };
    }

    /** Iron kept back for pickaxe, sword, shield and bucket that are still missing. */
    public static int reservedIron(Snapshot s) {
        int r = 0;
        if (ItemInfo.bestTier(s, "pickaxe") < ItemInfo.IRON) r += 3;
        if (ItemInfo.bestTier(s, "sword") < ItemInfo.IRON) r += 2;
        if (!s.has("shield") && !"shield".equals(s.offhand)) r += 1;
        if (!s.has("bucket") && !s.has("water_bucket") && !s.has("lava_bucket")) r += 3;
        return r;
    }

    /** Diamonds kept back for a diamond pickaxe and sword. */
    public static int reservedDiamonds(Snapshot s) {
        int r = 0;
        if (ItemInfo.bestTier(s, "pickaxe") < ItemInfo.DIAMOND) r += 3;
        if (ItemInfo.bestTier(s, "sword") < ItemInfo.DIAMOND) r += 2;
        return r;
    }

    /** The armor piece to craft next, or empty if nothing is affordable and better than what's owned. */
    public static Optional<String> nextPiece(Snapshot s) {
        Optional<String> diamond = pick(s, "diamond", ItemInfo.DIAMOND, s.count("diamond") - reservedDiamonds(s));
        if (diamond.isPresent()) return diamond;
        return pick(s, "iron", ItemInfo.IRON, s.count("iron_ingot") - reservedIron(s));
    }

    private static Optional<String> pick(Snapshot s, String material, int tier, int available) {
        for (String slot : ORDER) {
            int owned = ItemInfo.bestArmorTier(s, slot);
            boolean better = owned < tier || owned == ItemInfo.GOLD;
            if (better && available >= cost(slot)) return Optional.of(material + "_" + pieceName(slot));
        }
        return Optional.empty();
    }

    /**
     * @return the carried piece to put on in this slot, or "" to keep what's worn.
     */
    public static String bestToWear(String slot, String worn, List<String> carried, String dimension) {
        boolean nether = "the_nether".equals(dimension);
        if (nether && slot.equals("feet")) {
            if ("golden_boots".equals(worn)) return "";
            if (carried.contains("golden_boots")) return "golden_boots";
        }
        String best = "";
        int bestPoints = worn == null || worn.isEmpty() ? 0 : ItemInfo.armorPoints(worn);
        for (String id : carried) {
            if (!slot.equals(ItemInfo.armorSlot(id))) continue;
            int p = ItemInfo.armorPoints(id);
            if (p > bestPoints) {
                best = id;
                bestPoints = p;
            }
        }
        return best;
    }
}
