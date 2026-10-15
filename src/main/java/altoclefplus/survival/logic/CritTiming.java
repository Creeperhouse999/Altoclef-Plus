package altoclefplus.survival.logic;

import java.util.Map;

/** Jump-attack critical hits: jump with a full cooldown, hit on the way down. */
public final class CritTiming {
    private static final double FALLING = -0.08;
    private static final float READY = 0.9f;

    private CritTiming() {}

    public static boolean shouldJump(boolean onGround, float cooldown, double distance, double reach) {
        return onGround && cooldown >= READY && distance <= reach;
    }

    public static boolean shouldAttack(boolean onGround, double velocityY, float cooldown, double distance, double reach) {
        return !onGround && velocityY < FALLING && cooldown >= READY && distance <= reach;
    }

    /** Highest base damage melee weapon carried (axes hit harder per swing; ties go to swords). "" if none. */
    public static String bestWeapon(Map<String, Integer> items) {
        String best = "";
        double bestDamage = 0;
        for (String id : items.keySet()) {
            double d = damage(id);
            boolean sword = id.endsWith("_sword");
            if (d > bestDamage || (d == bestDamage && d > 0 && sword)) {
                best = id;
                bestDamage = d;
            }
        }
        return best;
    }

    /**
     * Situational weapon: an axe against a shield (it knocks the shield out) or when one axe crit finishes the
     * target and a sword crit wouldn't; a sword for groups (sweeping); otherwise the most damage per second.
     */
    public static String chooseWeapon(Map<String, Integer> items, int hostilesNear, boolean targetBlocking, float targetHealth) {
        String sword = best(items, "_sword");
        String axe = best(items, "_axe");
        if (targetBlocking && !axe.isEmpty()) return axe;
        if (hostilesNear >= 2 && !sword.isEmpty()) return sword;
        if (!axe.isEmpty() && !sword.isEmpty()) {
            boolean axeKills = damage(axe) * CRIT >= targetHealth;
            boolean swordKills = damage(sword) * CRIT >= targetHealth;
            if (axeKills && !swordKills) return axe;
            return damage(sword) * speed(sword) >= damage(axe) * speed(axe) ? sword : axe;
        }
        return sword.isEmpty() ? axe : sword;
    }

    /** Jump crits only where a jump is safe and actually crits (not swimming, climbing, riding, levitating). */
    public static boolean canCrit(boolean inWater, boolean inLava, boolean climbing, boolean riding, boolean levitating, boolean headroom) {
        return !inWater && !inLava && !climbing && !riding && !levitating && headroom;
    }

    private static final double CRIT = 1.5;

    private static String best(Map<String, Integer> items, String suffix) {
        String best = "";
        for (String id : items.keySet()) {
            if (id.endsWith(suffix) && damage(id) > damage(best)) best = id;
        }
        return best;
    }

    /** Attacks per second. */
    static double speed(String id) {
        if (id.endsWith("_sword")) return 1.6;
        return switch (id) {
            case "wooden_axe", "stone_axe" -> 0.8;
            case "iron_axe" -> 0.9;
            default -> 1.0;
        };
    }

    static double damage(String id) {
        return switch (id) {
            case "wooden_sword", "golden_sword" -> 4;
            case "stone_sword" -> 5;
            case "iron_sword" -> 6;
            case "diamond_sword" -> 7;
            case "netherite_sword" -> 8;
            case "wooden_axe", "golden_axe" -> 7;
            case "stone_axe", "iron_axe", "diamond_axe" -> 9;
            case "netherite_axe" -> 10;
            default -> 0;
        };
    }
}
