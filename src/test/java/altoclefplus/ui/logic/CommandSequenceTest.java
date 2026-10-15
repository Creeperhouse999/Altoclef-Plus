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

    @Test
    void redoesStepsWhoseItemsWereLostThenCompletes() {
        FakeExecutor ex = new FakeExecutor();
        java.util.Set<String> have = new java.util.HashSet<>();
        int[] completed = {0};
        CommandSequence seq = new CommandSequence(ex, have::contains, 2, () -> completed[0]++);
        seq.run("a;b;c");
        have.add("a");
        ex.finishers.get(0).run();
        have.add("b");
        ex.finishers.get(1).run();
        have.remove("a"); // lost "a" (died) while doing "c"
        have.add("c");
        ex.finishers.get(2).run();

        assertEquals(List.of("a", "b", "c", "a"), ex.started, "goes back for the missing step");
        assertEquals(0, completed[0]);
        have.add("a");
        ex.finishers.get(3).run();
        assertEquals(1, completed[0], "everything there: completion hook runs once");
        assertEquals(4, ex.started.size());
    }

    @Test
    void givesUpRedoingAfterMaxRounds() {
        FakeExecutor ex = new FakeExecutor();
        int[] completed = {0};
        CommandSequence seq = new CommandSequence(ex, c -> false, 2, () -> completed[0]++);
        seq.run("a");
        for (int i = 0; i < 10 && i < ex.finishers.size(); i++) ex.finishers.get(i).run();

        assertEquals(List.of("a", "a", "a"), ex.started, "1 run + 2 retries, never endless");
        assertEquals(1, completed[0]);
    }

    @Test
    void cancelledSequenceNeverCompletes() {
        FakeExecutor ex = new FakeExecutor();
        int[] completed = {0};
        CommandSequence seq = new CommandSequence(ex, c -> true, 2, () -> completed[0]++);
        seq.run("a");
        seq.cancel();
        ex.finishers.get(0).run();
        assertEquals(0, completed[0]);
    }
}
