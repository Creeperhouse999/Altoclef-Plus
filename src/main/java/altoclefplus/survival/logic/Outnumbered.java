package altoclefplus.survival.logic;

/**
 * Run when outnumbered: with this many hostile mobs close (8 blocks) the bot runs away at once, and keeps
 * running until fewer than 2 are within 16 blocks. AltoClef's own rule thought a stone sword could take on
 * five zombies in a cave.
 */
public final class Outnumbered {
    private final int threshold;
    private final long maxRunTicks;
    private final long cooldownTicks;
    private boolean running;
    private long runStart;
    private long restUntil = Long.MIN_VALUE / 2;

    /**
     * @param maxRunTicks   give up running after this long (a cave full of mobs never gets "clear")
     * @param cooldownTicks then don't start running again for this long (AltoClef fights or towers up)
     */
    public Outnumbered(int threshold, long maxRunTicks, long cooldownTicks) {
        this.threshold = threshold;
        this.maxRunTicks = maxRunTicks;
        this.cooldownTicks = cooldownTicks;
    }

    /** @param close hostiles within 8 blocks; @param around hostiles within 16 blocks */
    public boolean shouldRun(int close, int around, long tick) {
        if (running) {
            running = around >= 2;
            if (running && tick - runStart > maxRunTicks) {
                running = false;
                restUntil = tick + cooldownTicks;
            }
        } else if (tick >= restUntil && close >= threshold) {
            running = true;
            runStart = tick;
        }
        return running;
    }
}
