package altoclefplus.ui;

import adris.altoclef.AltoClef;
import altoclefplus.Plus;
import altoclefplus.ui.logic.CommandRunner;

/** Runs commands through AltoClef's executor directly (chat is never involved). */
public final class AltoClefCommandRunner implements CommandRunner {
    private final AltoClef mod;

    public AltoClefCommandRunner(AltoClef mod) {
        this.mod = mod;
    }

    @Override
    public void run(String command) {
        String prefix = mod.getModSettings().getCommandPrefix();
        String line = command.startsWith(prefix) ? command : prefix + command;
        Plus.log().add("plus", "Running: " + command);
        AltoClef.getCommandExecutor().execute(line, ex -> Plus.log().add("altoclef", ex.getMessage()));
    }

    @Override
    public void cancel() {
        mod.getUserTaskChain().cancel(mod);
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
        mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }
}
