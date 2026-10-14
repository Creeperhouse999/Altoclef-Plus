package altoclefplus.ui.logic;

/**
 * Start / stop / pause / resume state machine. AltoClef cannot freeze a task, so pause cancels the task
 * and remembers its command; resume re-runs it.
 */
public final class BotControl {
    private final CommandRunner runner;
    private BotState state = BotState.IDLE;
    private String lastCommand = "";
    /** True while we cancel on purpose (pause/restart) so the resulting "finished" event is ignored. */
    private boolean cancelling;

    public BotControl(CommandRunner runner) {
        this.runner = runner;
    }

    public void start(String command) {
        lastCommand = command;
        state = BotState.RUNNING;
        runner.run(command);
    }

    /** A task was started some other way (e.g. by a preset chain); track it without re-running. */
    public void onExternalStart(String command) {
        lastCommand = command;
        state = BotState.RUNNING;
    }

    public void stop() {
        cancelQuietly();
        state = BotState.IDLE;
        lastCommand = "";
    }

    public void togglePause() {
        switch (state) {
            case RUNNING, RECOVERING -> {
                cancelQuietly();
                state = BotState.PAUSED;
            }
            case PAUSED -> {
                state = BotState.RUNNING;
                runner.run(lastCommand);
            }
            case IDLE -> { }
        }
    }

    /** Cancel and immediately re-run the current command (used by stuck recovery). */
    public void restart() {
        if (lastCommand.isEmpty()) return;
        cancelQuietly();
        state = BotState.RUNNING;
        runner.run(lastCommand);
    }

    public void onTaskFinished() {
        if (cancelling) return;
        if (state == BotState.RUNNING || state == BotState.RECOVERING) state = BotState.IDLE;
    }

    public void setRecovering(boolean recovering) {
        if (recovering && state == BotState.RUNNING) state = BotState.RECOVERING;
        else if (!recovering && state == BotState.RECOVERING) state = BotState.RUNNING;
    }

    public BotState state() {
        return state;
    }

    public String lastCommand() {
        return lastCommand;
    }

    private void cancelQuietly() {
        cancelling = true;
        try {
            runner.cancel();
        } finally {
            cancelling = false;
        }
    }
}
