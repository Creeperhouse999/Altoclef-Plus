package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static altoclefplus.survival.logic.IdleGuard.Action.*;
import static org.junit.jupiter.api.Assertions.*;

class IdleGuardTest {
    @Test
    void engagesOnThreatWhileIdleAndAfk() {
        IdleGuard g = new IdleGuard(40);
        assertEquals(NONE, g.tick(true, false, false, false));
        assertEquals(ENGAGE, g.tick(true, false, false, true));
        assertEquals(NONE, g.tick(true, false, false, true), "engages once");
        assertTrue(g.engaged());
    }

    @Test
    void releasesAfterCalmPeriod() {
        IdleGuard g = new IdleGuard(3);
        g.tick(true, false, false, true);
        assertEquals(NONE, g.tick(true, false, false, false));
        assertEquals(NONE, g.tick(true, false, false, true), "threat again resets the calm timer");
        assertEquals(NONE, g.tick(true, false, false, false));
        assertEquals(NONE, g.tick(true, false, false, false));
        assertEquals(RELEASE, g.tick(true, false, false, false));
        assertFalse(g.engaged());
    }

    @Test
    void neverTakesOverWhileThePlayerIsPlaying() {
        IdleGuard g = new IdleGuard(40);
        assertEquals(NONE, g.tick(true, false, true, true));
        assertFalse(g.engaged());
    }

    @Test
    void handsBackWhenThePlayerStartsPlayingOrSettingIsOff() {
        IdleGuard g = new IdleGuard(40);
        g.tick(true, false, false, true);
        assertEquals(RELEASE, g.tick(true, false, true, true));

        IdleGuard off = new IdleGuard(40);
        off.tick(true, false, false, true);
        assertEquals(RELEASE, off.tick(false, false, false, true));
    }

    @Test
    void userTaskTakesOverWithoutRelease() {
        IdleGuard g = new IdleGuard(40);
        g.tick(true, false, false, true);
        // A real task started: AltoClef owns the runner now, we must not switch it off.
        assertEquals(NONE, g.tick(true, true, false, true));
        assertFalse(g.engaged());
        assertEquals(NONE, g.tick(true, true, false, true), "never engages during a task");
    }
}
