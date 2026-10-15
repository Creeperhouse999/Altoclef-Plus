package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TargetStickinessTest {
    @Test
    void keepsCurrentTargetWhenNewOneIsOnlySlightlyCloser() {
        TargetStickiness t = new TargetStickiness(60);
        assertTrue(t.shouldSwitch(false, 0, 9, 0), "no current target: take the new one");
        t.switched(0);
        assertFalse(t.shouldSwitch(true, 16, 12, 100), "4 vs 3 blocks away: stay on the current block");
    }

    @Test
    void switchesWhenNewTargetIsMuchCloserButNotTooOften() {
        TargetStickiness t = new TargetStickiness(60);
        t.switched(0);
        assertFalse(t.shouldSwitch(true, 100, 4, 30), "much closer, but we just switched (spam guard)");
        assertTrue(t.shouldSwitch(true, 100, 4, 61), "much closer (10 vs 2 blocks) after the cooldown");
    }

    @Test
    void alwaysSwitchesAwayFromAnInvalidTarget() {
        TargetStickiness t = new TargetStickiness(60);
        t.switched(0);
        assertTrue(t.shouldSwitch(false, 0, 50, 1), "current block gone/blacklisted: switch right away");
    }
}
