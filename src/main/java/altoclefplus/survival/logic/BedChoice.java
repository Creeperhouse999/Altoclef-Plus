package altoclefplus.survival.logic;

/**
 * AltoClef went to sleep in any bed it had ever seen, even 370 blocks away underground at night, while carrying
 * a stack of beds. With a bed in the inventory, only beds within 32 blocks count; otherwise it places its own.
 */
public final class BedChoice {
    public static final double NEAR = 32;

    private BedChoice() {}

    public static boolean useKnownBed(double distance, boolean carryingBed) {
        return !carryingBed || distance <= NEAR;
    }
}
