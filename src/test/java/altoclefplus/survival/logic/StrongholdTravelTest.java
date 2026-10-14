package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StrongholdTravelTest {
    @Test
    void intersectsTwoThrowsOnXZ() {
        // throw 1 from (0,0) heading +x; throw 2 from (100,-100) heading +z -> meet at (100, 0)
        Optional<double[]> p = StrongholdTravel.intersectXZ(0, 0, 1, 0, 100, -100, 0, 1, 1.0, 3000);
        assertTrue(p.isPresent());
        assertEquals(100, p.get()[0], 1e-6);
        assertEquals(0, p.get()[1], 1e-6);
    }

    @Test
    void rejectsNearlyParallelThrows() {
        assertTrue(StrongholdTravel.intersectXZ(0, 0, 1, 0, 0, 30, 1, 0.001, 1.0, 3000).isEmpty());
    }

    @Test
    void rejectsIntersectionBehindAThrow() {
        // lines cross at (100,0) but throw 2 points away from it (-z from z=-100)
        assertTrue(StrongholdTravel.intersectXZ(0, 0, 1, 0, 100, -100, 0, -1, 1.0, 3000).isEmpty());
    }

    @Test
    void rejectsAbsurdlyFarEstimates() {
        // throws 100 blocks apart, almost parallel (1.15 degrees): they meet ~5000 blocks away
        assertTrue(StrongholdTravel.intersectXZ(0, 0, 1, 0, 0, -100, 1, 0.02, 1.0, 3000).isEmpty());
    }

    @Test
    void netherCoordinatesFloorNegativeValues() {
        assertEquals(2, StrongholdTravel.netherCoord(17));
        assertEquals(-3, StrongholdTravel.netherCoord(-17));
        assertEquals(-1, StrongholdTravel.netherCoord(-8));
        assertEquals(0, StrongholdTravel.netherCoord(7));
    }

    @Test
    void usesNetherOnlyForLongTripsAndWhenAllowed() {
        assertTrue(StrongholdTravel.useNether(1500, 800, true));
        assertFalse(StrongholdTravel.useNether(500, 800, true));
        assertFalse(StrongholdTravel.useNether(5000, 800, false));
    }
}
