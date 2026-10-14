package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoatTravelTest {
    /** land for {@code before} blocks, water for {@code water}, then land. */
    private static List<Boolean> line(int before, int water, int total) {
        List<Boolean> l = new ArrayList<>();
        for (int i = 0; i < total; i++) l.add(i >= before && i < before + water);
        return l;
    }

    @Test
    void oceanCrossingUsesBoat() {
        assertTrue(BoatTravel.shouldBoat(line(1, 40, 64), 300, 24, 3, 48));
    }

    @Test
    void narrowRiverIsWalkedOrSwum() {
        assertFalse(BoatTravel.shouldBoat(line(1, 4, 64), 300, 24, 3, 48));
    }

    @Test
    void smallLakeIsNotWorthABoat() {
        assertFalse(BoatTravel.shouldBoat(line(0, 20, 64), 300, 24, 3, 48));
    }

    @Test
    void waterTooFarAheadIsIgnoredForNow() {
        assertFalse(BoatTravel.shouldBoat(line(5, 40, 64), 300, 24, 3, 48));
    }

    @Test
    void shortTripsDoNotBotherWithBoats() {
        assertFalse(BoatTravel.shouldBoat(line(1, 40, 64), 30, 24, 3, 48));
    }

    @Test
    void steeringTurnsTowardTheTarget() {
        assertEquals(1, BoatTravel.turnInput(0, 30, 5), "target clockwise -> press right");
        assertEquals(-1, BoatTravel.turnInput(0, -30, 5));
        assertEquals(0, BoatTravel.turnInput(0, 3, 5), "inside dead zone");
        assertEquals(-1, BoatTravel.turnInput(170, -170, 5) * -1, "short way across the seam (+20 -> right)");
    }

    @Test
    void stopsAtLandOrEndOfWater() {
        assertTrue(BoatTravel.shouldStop(true, 10));
        assertTrue(BoatTravel.shouldStop(false, 1));
        assertFalse(BoatTravel.shouldStop(false, 10));
    }
}
