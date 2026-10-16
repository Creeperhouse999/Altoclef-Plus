package altoclefplus.ui.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BotControlTest {
    /** Records calls; like AltoClef, cancelling fires the "task finished" callback synchronously. */
    static class FakeRunner implements CommandRunner {
        final List<String> calls = new ArrayList<>();
        BotControl control;

        @Override public void run(String command) { calls.add("run " + command); }
        @Override public void cancel() {
            calls.add("cancel");
            if (control != null) control.onTaskFinished();
        }
    }

    FakeRunner runner;
    BotControl control;

    @BeforeEach
    void setUp() {
        runner = new FakeRunner();
        control = new BotControl(runner);
        runner.control = control;
    }

    @Test
    void startRunsCommandAndIsRunning() {
        control.start("gamer");

        assertEquals(BotState.RUNNING, control.state());
        assertEquals(List.of("run gamer"), runner.calls);
        assertEquals("gamer", control.lastCommand());
    }

    @Test
    void pauseCancelsButKeepsCommand() {
        control.start("get diamond 3");
        control.togglePause();

        assertEquals(BotState.PAUSED, control.state());
        assertEquals("get diamond 3", control.lastCommand());
        assertEquals(List.of("run get diamond 3", "cancel"), runner.calls);
    }

    @Test
    void resumeRerunsSameCommand() {
        control.start("gamer");
        control.togglePause();
        control.togglePause();

        assertEquals(BotState.RUNNING, control.state());
        assertEquals(List.of("run gamer", "cancel", "run gamer"), runner.calls);
    }

    @Test
    void stopFromPausedForgetsCommandSoResumeDoesNothing() {
        control.start("gamer");
        control.togglePause();
        control.stop();
        runner.calls.clear();

        control.togglePause();

        assertEquals(BotState.IDLE, control.state());
        assertEquals("", control.lastCommand());
        assertTrue(runner.calls.isEmpty());
    }

    @Test
    void taskFinishingWhileRunningGoesIdle() {
        control.start("gamer");
        control.onTaskFinished();

        assertEquals(BotState.IDLE, control.state());
    }

    @Test
    void restartKeepsRunningEvenThoughCancelFiresFinish() {
        control.start("gamer");
        control.restart();

        assertEquals(BotState.RUNNING, control.state());
        assertEquals(List.of("run gamer", "cancel", "run gamer"), runner.calls);
    }

    @Test
    void recoveringIsShownOnlyWhileRunning() {
        control.setRecovering(true);
        assertEquals(BotState.IDLE, control.state());

        control.start("gamer");
        control.setRecovering(true);
        assertEquals(BotState.RECOVERING, control.state());

        control.setRecovering(false);
        assertEquals(BotState.RUNNING, control.state());
    }

    @Test
    void externallyStartedTaskIsTrackedAsRunning() {
        control.onExternalStart("get iron_ingot 10");

        assertEquals(BotState.RUNNING, control.state());
        assertEquals("get iron_ingot 10", control.lastCommand());
        assertTrue(runner.calls.isEmpty());
    }

    @Test
    void aGetForItemsAltoClefCannotFetchDoesNotStart() {
        BotControl c = new BotControl(runner, cmd -> GetCheck.problem(cmd, id -> !id.equals("grass_block") && !id.equals("cogwheel")));
        String problem = c.start("get [dirt 64, grass_block 1, cogwheel 2]");
        assertNotNull(problem);
        assertTrue(problem.contains("grass block") && problem.contains("cogwheel"), problem);
        assertEquals(BotState.IDLE, c.state(), "not shown as running while nothing happens");
        assertTrue(runner.calls.isEmpty());
        assertNull(c.start("get dirt 64"));
        assertEquals(BotState.RUNNING, c.state());
        assertNull(GetCheck.problem("gamer", id -> false), "only get commands are checked");
    }
}
