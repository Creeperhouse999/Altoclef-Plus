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
