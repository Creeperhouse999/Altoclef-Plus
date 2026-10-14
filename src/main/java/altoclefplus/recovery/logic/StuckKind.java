package altoclefplus.recovery.logic;

public enum StuckKind {
    NO_PROGRESS("no progress"),
    PILLAR("pillaring"),
    SAME_BLOCK("same block"),
    FLIP_FLOP("walking back and forth"),
    RESTART_LOOP("task keeps restarting"),
    DIMENSION_PING_PONG("Nether back-and-forth");

    public final String label;

    StuckKind(String label) {
        this.label = label;
    }
}
