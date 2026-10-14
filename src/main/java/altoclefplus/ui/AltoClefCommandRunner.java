package altoclefplus.ui;

import adris.altoclef.AltoClef;
import altoclefplus.Plus;
import altoclefplus.ui.logic.CommandRunner;
import altoclefplus.ui.logic.CommandSequence;

/**
 * Runs commands through AltoClef's executor directly (chat is never involved). ';' lists are run one part at
 * a time by {@link CommandSequence}, so Stop and Pause also stop the remaining parts.
 */
public final class AltoClefCommandRunner implements CommandRunner {
    private final AltoClef mod;
    private final CommandSequence sequence;

    public AltoClefCommandRunner(AltoClef mod) {
        this.mod = mod;
        this.sequence = new CommandSequence(this::executeOne);
    }

    @Override
    public void run(String command) {
        Plus.log().add("plus", "Running: " + command);
        sequence.run(command);
    }

    private void executeOne(String command, Runnable onFinish) {
        String prefix = mod.getModSettings().getCommandPrefix();
        String line = command.startsWith(prefix) ? command : prefix + command;
        AltoClef.getCommandExecutor().execute(line, onFinish, ex -> Plus.log().add("altoclef", ex.getMessage()));
    }

    @Override
    public void cancel() {
        sequence.cancel();
        mod.getUserTaskChain().cancel(mod);
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
        mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }
}
