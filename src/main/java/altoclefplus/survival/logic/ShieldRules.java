package altoclefplus.survival.logic;

/**
 * Holding right-click uses the MAIN-hand item first and interacts with whatever the crosshair is on. The
 * offhand shield is only raised when that can't do anything else: a sword or empty hand, aiming at nothing
 * or at a hostile mob (no lava buckets, pearls, flint and steel, log stripping, doors, beds or boats).
 */
public final class ShieldRules {
    public enum Target { NOTHING, BLOCK, HOSTILE, OTHER_ENTITY }

    private ShieldRules() {}

    public static boolean canRaise(String mainHandId, Target target) {
        boolean harmlessHand = mainHandId == null || mainHandId.isEmpty() || mainHandId.endsWith("_sword");
        boolean harmlessTarget = target == Target.NOTHING || target == Target.HOSTILE;
        return harmlessHand && harmlessTarget;
    }
}
