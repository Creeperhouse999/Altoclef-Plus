package altoclefplus.build;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import altoclefplus.build.logic.BuildJob;

/**
 * "plusbuild &lt;job id&gt;" - runs a build job from the Build tab. Going through an AltoClef command means the
 * usual Pause/Resume/Stop work on builds too.
 */
public final class BuildCommand extends Command {
    public BuildCommand() throws CommandException {
        super("plusbuild", "Build a schematic job from the AltoClef Plus Build tab", new Arg<>(String.class, "job"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String id = parser.get(String.class);
        BuildJob job = Builds.find(id);
        if (job == null) {
            finish();
            throw new CommandException("No build job " + id);
        }
        BuildTask task = new BuildTask(job);
        Builds.onStarted(job);
        mod.runUserTask(task, () -> {
            Builds.onEnded(task);
            finish();
        });
    }
}
