package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

/** "huntendermen [count]" - used by the panel's "Hunt endermen" button. */
public final class HuntEndermenCommand extends Command {
    public HuntEndermenCommand() throws CommandException {
        super("huntendermen", "Get ender pearls with a boat trap in a warped forest",
                new Arg<>(Integer.class, "count", 12, 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        int count = parser.get(Integer.class);
        mod.runUserTask(new EndermanBoatHuntTask(count), this::finish);
    }
}
