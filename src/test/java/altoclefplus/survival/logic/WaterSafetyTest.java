package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static altoclefplus.survival.logic.WaterSafety.AirAction.*;
import static org.junit.jupiter.api.Assertions.*;

class WaterSafetyTest {
    @Test
    void nothingWhileAirIsFineOrNotUnderwater() {
        assertEquals(NONE, WaterSafety.airAction(250, true, 3, true));
        assertEquals(NONE, WaterSafety.airAction(50, false, 3, true), "head above water");
    }

    @Test
    void swimsUpWhenTheSurfaceIsCloseEnough() {
        assertEquals(SWIM_UP, WaterSafety.airAction(110, true, 4, true));
    }

    @Test
    void doorPocketWhenSurfaceIsFarOrBlocked() {
        assertEquals(DOOR_POCKET, WaterSafety.airAction(110, true, -1, true), "ceiling above");
        assertEquals(DOOR_POCKET, WaterSafety.airAction(60, true, 12, true), "too far up for the air left");
        assertEquals(NONE, WaterSafety.airAction(60, true, -1, false), "ceiling and no door: don't take over, let pathing get out");
        assertEquals(SWIM_UP, WaterSafety.airAction(60, true, 12, false), "far but open surface and no door: swim up");
    }

    @Test
    void caveWaterCostsMoreUndergroundOnly() {
        assertEquals(0, WaterSafety.extraCost(false, false, true, 40));
        assertEquals(0, WaterSafety.extraCost(true, false, false, 40), "open-sky water: normal cost");
        assertEquals(40, WaterSafety.extraCost(true, false, true, 40));
        assertEquals(100, WaterSafety.extraCost(true, true, true, 40), "head underwater in a cave: much worse");
    }
}
