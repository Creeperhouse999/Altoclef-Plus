package altoclefplus.ui.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandSequenceTest {
    /** Records started commands; the test decides when each one "finishes". */
    static class FakeExecutor implements CommandSequence.Executor {
        final List<String> started = new ArrayList<>();
        final List<Runnable> finishers = new ArrayList<>();

        @Override
        public void execute(String command, Runnable onFinish) {
            started.add(command);
            finishers.add(onFinish);
        }
    }

    @Test
    void runsPartsOneAfterAnother() {
        FakeExecutor ex = new FakeExecutor();
        CommandSequence seq = new CommandSequence(ex);
        seq.run("get iron_pickaxe 1; get iron_sword 1");

        assertEquals(List.of("get iron_pickaxe 1"), ex.started);
        ex.finishers.get(0).run();
        assertEquals(List.of("get iron_pickaxe 1", "get iron_sword 1"), ex.started);
    }

    @Test
    void cancelStopsTheRemainingParts() {
        FakeExecutor ex = new FakeExecutor();
        CommandSequence seq = new CommandSequence(ex);
        seq.run("a;b;c");
        seq.cancel();
        ex.finishers.get(0).run(); // AltoClef reports the cancelled part as finished

        assertEquals(List.of("a"), ex.started);
    }

    @Test
    void aNewRunReplacesTheOldSequence() {
        FakeExecutor ex = new FakeExecutor();
        CommandSequence seq = new CommandSequence(ex);
        seq.run("a;b");
        seq.run("x");
        ex.finishers.get(0).run(); // late finish of "a" must not start "b"

        assertEquals(List.of("a", "x"), ex.started);
    }

    @Test
    void blankPartsAreSkipped() {
        FakeExecutor ex = new FakeExecutor();
        new CommandSequence(ex).run(" ; food 5 ;");
        assertEquals(List.of("food 5"), ex.started);
    }
}
