package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutnumberedTest {
    @Test
    void runsWhenOutnumberedAndKeepsRunningUntilClear() {
        Outnumbered o = new Outnumbered(3);
        assertFalse(o.shouldRun(2, 2), "two mobs: fight as usual");
        assertTrue(o.shouldRun(3, 3), "three close: run");
        assertTrue(o.shouldRun(1, 3), "still 3 within 16: keep running");
        assertTrue(o.shouldRun(0, 2), "two still chasing: keep running");
        assertFalse(o.shouldRun(0, 1), "only one left: back to the task");
        assertFalse(o.shouldRun(2, 2), "and it doesn't restart for two");
    }
}
