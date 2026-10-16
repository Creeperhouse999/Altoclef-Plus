package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwimAndThreatTest {
    @Test
    void swimsLikeAPlayerInDeepWaterInsteadOfBobbing() {
        assertTrue(SwimPolicy.swim(true, 3, 300, 300, false, true));
        assertFalse(SwimPolicy.swim(true, 1, 300, 300, false, true), "shallow: just walk/jump");
        assertFalse(SwimPolicy.swim(true, 3, 100, 300, false, true), "low on air: go up");
        assertFalse(SwimPolicy.swim(true, 3, 300, 300, true, true), "a block ahead to climb out onto");
        assertFalse(SwimPolicy.swim(true, 3, 300, 300, false, false), "not moving forward");
        assertFalse(SwimPolicy.swim(false, 3, 300, 300, false, true), "not in water");
    }

    @Test
    void onlyMobsCountAsAThreatNotFallsOrHunger() {
        assertTrue(SwimPolicy.threat(true, false), "a hostile it can see nearby");
        assertTrue(SwimPolicy.threat(false, true), "a hostile right next to it (maybe behind)");
        assertFalse(SwimPolicy.threat(false, false), "hurt by a fall or starving: not 'Defending'");
    }
}
