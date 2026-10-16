package altoclefplus.recovery.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DangerAreasTest {
    @Test
    void avoidsAroundTheSpotUntilItExpires() {
        DangerAreas d = new DangerAreas();
        d.add("overworld", 100, 40, 100, 10, 600_000, 0);
        assertTrue(d.avoid("overworld", 105, 42, 98, 1_000), "inside 10 blocks");
        assertFalse(d.avoid("overworld", 115, 40, 100, 1_000), "outside");
        assertFalse(d.avoid("the_nether", 100, 40, 100, 1_000), "other dimension");
        assertFalse(d.avoid("overworld", 100, 40, 100, 600_001), "expired after 10 minutes");
    }

    @Test
    void neverAvoidsWhereTheBotStands() {
        DangerAreas d = new DangerAreas();
        d.add("overworld", 0, 64, 0, 10, 600_000, 0);
        d.setStandingAt("overworld", 2, 64, 1);
        assertFalse(d.avoid("overworld", 2, 64, 1, 5), "its own cell stays walkable so it can leave");
        assertTrue(d.avoid("overworld", 0, 64, 0, 5));
    }

    @Test
    void insideAnAreaItCanWalkOutButNotDeeper() {
        DangerAreas d = new DangerAreas();
        d.add("overworld", 0, 64, 0, 32, 1000, 0);
        d.setStandingAt("overworld", 10, 64, 0);
        assertFalse(d.avoid("overworld", 11, 64, 0, 5), "one step outward is allowed");
        assertFalse(d.avoid("overworld", 10, 64, 1, 5), "sideways is allowed");
        assertTrue(d.avoid("overworld", 5, 64, 0, 5), "back towards where it died is not");
        assertTrue(d.avoid("overworld", 0, 64, 0, 5));
    }
}
