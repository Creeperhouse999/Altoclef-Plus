package altoclefplus.survival.logic;

/**
 * Run when outnumbered: with this many hostile mobs close (8 blocks) the bot runs away at once, and keeps
 * running until fewer than 2 are within 16 blocks. AltoClef's own rule thought a stone sword could take on
 * five zombies in a cave.
 */
public final class Outnumbered {
    private final int threshold;
    private boolean running;

    public Outnumbered(int threshold) {
        this.threshold = threshold;
    }

    /** @param close hostiles within 8 blocks; @param around hostiles within 16 blocks */
    public boolean shouldRun(int close, int around) {
        if (running) {
            running = around >= 2;
        } else {
            running = close >= threshold;
        }
        return running;
    }
}
