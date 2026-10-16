package altoclefplus.recovery.logic;

/**
 * How big an area to avoid after a death. Pillagers live in outposts and patrols and shoot from range, so a
 * single death next to them is enough; anything else needs repeated deaths in one place first.
 */
public final class DeathDanger {
    /** Pillagers shoot from about 16 blocks and an outpost's guards spread out around the tower. */
    public static final int PILLAGER_RADIUS = 48;

    private DeathDanger() {}

    /** @return blocks to avoid around the spot, or 0 for none */
    public static int avoidRadius(int pillagersNear, boolean deathLoop, int loopRadius) {
        if (pillagersNear > 0) return PILLAGER_RADIUS;
        return deathLoop ? loopRadius : 0;
    }
}
