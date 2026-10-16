package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutnumberedTest {
    @Test
    void runsWhenOutnumberedAndKeepsRunningUntilClear() {
        Outnumbered o = new Outnumbered(3, 600, 1200);
        assertFalse(o.shouldRun(2, 2, 0), "two mobs: fight as usual");
        assertTrue(o.shouldRun(3, 3, 1), "three close: run");
        assertTrue(o.shouldRun(1, 3, 2), "still 3 within 16: keep running");
        assertTrue(o.shouldRun(0, 2, 3), "two still chasing: keep running");
        assertFalse(o.shouldRun(0, 1, 4), "only one left: back to the task");
        assertFalse(o.shouldRun(2, 2, 5), "and it doesn't restart for two");
    }

    @Test
    void givesUpRunningAfterAWhileThenRestsBeforeRunningAgain() {
        Outnumbered o = new Outnumbered(3, 600, 1200);
        assertTrue(o.shouldRun(4, 4, 0));
        assertTrue(o.shouldRun(4, 4, 599));
        assertFalse(o.shouldRun(4, 4, 601), "ran 30 s without getting away: stop, let AltoClef deal with it");
        assertFalse(o.shouldRun(5, 5, 1500), "no new run during the cooldown");
        assertTrue(o.shouldRun(5, 5, 1802), "cooldown over");
    }

    @Test
    void anUnarmoredBotCountsAPillagerInRangeAsOutnumbered() {
        assertEquals(3, Outnumbered.effectiveClose(0, 1, false, 3), "crossbows outrange a sword");
        assertEquals(1, Outnumbered.effectiveClose(1, 1, true, 3), "with iron armor it may fight");
        assertEquals(1, Outnumbered.effectiveClose(1, 0, false, 3));
        assertEquals(5, Outnumbered.effectiveClose(5, 2, false, 3));
    }
}
