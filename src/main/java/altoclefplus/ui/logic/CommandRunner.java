package altoclefplus.ui.logic;

/** Runs/cancels AltoClef user commands. Implemented in-game by AltoClef's command executor. */
public interface CommandRunner {
    /** Runs a command without prefix, e.g. "get diamond 3" or "gamer". */
    void run(String command);

    /** Cancels the current user task and Baritone pathing. */
    void cancel();
}
