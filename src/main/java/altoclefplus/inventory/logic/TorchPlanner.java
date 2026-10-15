package altoclefplus.inventory.logic;

/** Torches (optional): light up dark places the bot works in, so mobs don't spawn and you can see. */
public final class TorchPlanner {
    /** Below this block light (and no sky light) it is dark enough for mobs to spawn. */
    private static final int DARK = 7;
    private static final int SPACING = 7;
    public static final int KEEP = 16;

    private boolean anyPlaced;
    private int lx, ly, lz;

    public boolean shouldPlace(boolean enabled, int blockLight, int skyLight, int x, int y, int z, boolean hasTorch) {
        if (!enabled || !hasTorch || blockLight >= DARK || skyLight >= DARK) return false;
        if (!anyPlaced) return true;
        long dx = x - lx, dy = y - ly, dz = z - lz;
        return dx * dx + dy * dy + dz * dz >= (long) SPACING * SPACING;
    }

    public void placed(int x, int y, int z) {
        anyPlaced = true;
        lx = x;
        ly = y;
        lz = z;
    }

    /** Craft more only from coal/charcoal already carried (and something for sticks). */
    public static boolean shouldCraft(boolean enabled, int torches, int coal, int woodOrSticks) {
        return enabled && torches < KEEP / 2 && coal > 0 && woodOrSticks > 0;
    }
}
