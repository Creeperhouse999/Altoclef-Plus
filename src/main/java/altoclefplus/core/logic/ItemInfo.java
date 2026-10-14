package altoclefplus.core.logic;

import java.util.Map;

/** Pure item facts by id ("iron_pickaxe"): tool type/tier, armor slot/points, food value, categories. */
public final class ItemInfo {
    public static final int NONE = 0;
    public static final int GOLD = 1;
    public static final int WOOD = 2;
    public static final int STONE = 3;
    public static final int IRON = 4;
    public static final int DIAMOND = 5;
    public static final int NETHERITE = 6;

    public static final String[] TOOL_TYPES = {"pickaxe", "axe", "shovel", "sword", "hoe"};
    public static final String[] ARMOR_SLOTS = {"head", "chest", "legs", "feet"};

    private static final Map<String, Integer> FOOD = Map.ofEntries(
            Map.entry("apple", 4), Map.entry("baked_potato", 5), Map.entry("beetroot", 1),
            Map.entry("beetroot_soup", 6), Map.entry("bread", 5), Map.entry("carrot", 3),
            Map.entry("cooked_beef", 8), Map.entry("cooked_chicken", 6), Map.entry("cooked_cod", 5),
            Map.entry("cooked_mutton", 6), Map.entry("cooked_porkchop", 8), Map.entry("cooked_rabbit", 5),
            Map.entry("cooked_salmon", 6), Map.entry("cookie", 2), Map.entry("dried_kelp", 1),
            Map.entry("golden_apple", 4), Map.entry("enchanted_golden_apple", 4), Map.entry("golden_carrot", 6),
            Map.entry("melon_slice", 2), Map.entry("mushroom_stew", 6), Map.entry("potato", 1),
            Map.entry("pumpkin_pie", 8), Map.entry("rabbit_stew", 10), Map.entry("beef", 3),
            Map.entry("chicken", 2), Map.entry("cod", 2), Map.entry("mutton", 2), Map.entry("porkchop", 3),
            Map.entry("rabbit", 3), Map.entry("salmon", 2), Map.entry("sweet_berries", 2),
            Map.entry("glow_berries", 2), Map.entry("honey_bottle", 6), Map.entry("suspicious_stew", 6));

    private ItemInfo() {}

    /** "pickaxe", "axe", "shovel", "sword", "hoe" or "". */
    public static String toolType(String id) {
        if (id == null) return "";
        for (String t : TOOL_TYPES) {
            if (id.endsWith("_" + t) && tierPrefix(id) != NONE) return t;
        }
        return "";
    }

    /** Material tier from the id prefix (wooden/stone/iron/...), NONE if not a tool/armor material. */
    public static int tier(String id) {
        if (id == null) return NONE;
        if (toolType(id).isEmpty() && armorSlot(id).isEmpty()) return NONE;
        return tierPrefix(id);
    }

    private static int tierPrefix(String id) {
        if (id.startsWith("wooden_")) return WOOD;
        if (id.startsWith("stone_")) return STONE;
        if (id.startsWith("iron_")) return IRON;
        if (id.startsWith("golden_")) return GOLD;
        if (id.startsWith("diamond_")) return DIAMOND;
        if (id.startsWith("netherite_")) return NETHERITE;
        if (id.startsWith("leather_") || id.startsWith("chainmail_") || id.startsWith("turtle_")) return GOLD;
        return NONE;
    }

    /** "head", "chest", "legs", "feet" or "". */
    public static String armorSlot(String id) {
        if (id == null) return "";
        if (id.endsWith("_helmet")) return "head";
        if (id.endsWith("_chestplate")) return "chest";
        if (id.endsWith("_leggings")) return "legs";
        if (id.endsWith("_boots")) return "feet";
        return "";
    }

    /** Vanilla armor points plus toughness, for comparing pieces of the same slot. */
    public static int armorPoints(String id) {
        String slot = armorSlot(id);
        if (slot.isEmpty()) return 0;
        int[] p; // head, chest, legs, feet
        if (id.startsWith("leather_")) p = new int[]{1, 3, 2, 1};
        else if (id.startsWith("golden_")) p = new int[]{2, 5, 3, 1};
        else if (id.startsWith("chainmail_")) p = new int[]{2, 5, 4, 1};
        else if (id.startsWith("iron_")) p = new int[]{2, 6, 5, 2};
        else if (id.startsWith("turtle_")) p = new int[]{2, 0, 0, 0};
        else if (id.startsWith("diamond_")) p = new int[]{5, 10, 8, 5};
        else if (id.startsWith("netherite_")) p = new int[]{6, 11, 9, 6};
        else return 0;
        return switch (slot) {
            case "head" -> p[0];
            case "chest" -> p[1];
            case "legs" -> p[2];
            default -> p[3];
        };
    }

    /** Best tier among carried tools of a type ("pickaxe"...). */
    public static int bestTier(Snapshot s, String toolType) {
        int best = NONE;
        for (String id : s.items.keySet()) {
            if (toolType.equals(toolType(id))) best = Math.max(best, tier(id));
        }
        return best;
    }

    /** Best tier for an armor slot among worn and carried pieces. */
    public static int bestArmorTier(Snapshot s, String slot) {
        int best = tierOrNone(s.armor.getOrDefault(slot, ""));
        for (String id : s.items.keySet()) {
            if (slot.equals(armorSlot(id))) best = Math.max(best, tier(id));
        }
        return best;
    }

    private static int tierOrNone(String id) {
        return id == null || id.isEmpty() ? NONE : tier(id);
    }

    public static int foodUnits(String id) {
        return FOOD.getOrDefault(id, 0);
    }

    public static boolean isFood(String id) {
        return FOOD.containsKey(id);
    }

    public static int totalFoodUnits(Snapshot s) {
        int total = 0;
        for (Map.Entry<String, Integer> e : s.items.entrySet()) total += foodUnits(e.getKey()) * e.getValue();
        return total;
    }

    public static boolean isLog(String id) {
        return id.endsWith("_log") || id.endsWith("_stem") || id.endsWith("_wood") || id.endsWith("_hyphae");
    }

    public static boolean isPlanks(String id) {
        return id.endsWith("_planks");
    }

    public static boolean isBed(String id) {
        return id.endsWith("_bed");
    }

    public static int countMatching(Snapshot s, java.util.function.Predicate<String> p) {
        int n = 0;
        for (Map.Entry<String, Integer> e : s.items.entrySet()) if (p.test(e.getKey())) n += e.getValue();
        return n;
    }
}
