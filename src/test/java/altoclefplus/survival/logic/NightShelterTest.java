package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NightShelterTest {
    @Test
    void sheltersAtNightWhenWeakAndCannotSleep() {
        assertTrue(NightShelter.shouldShelter(true, 13500, "overworld", 0, false, false, 4));
        assertFalse(NightShelter.shouldShelter(true, 6000, "overworld", 0, false, false, 4), "day");
        assertFalse(NightShelter.shouldShelter(true, 13500, "overworld", 4, false, false, 4), "iron armor: can handle the night");
        assertFalse(NightShelter.shouldShelter(true, 13500, "overworld", 0, true, false, 4), "can sleep instead");
        assertFalse(NightShelter.shouldShelter(true, 13500, "the_nether", 0, false, false, 4), "no night in the Nether");
        assertFalse(NightShelter.shouldShelter(true, 13500, "overworld", 0, false, true, 4), "in water");
        assertFalse(NightShelter.shouldShelter(true, 13500, "overworld", 0, false, false, 0), "no block to cover the hole");
        assertFalse(NightShelter.shouldShelter(false, 13500, "overworld", 0, false, false, 4), "setting off");
    }

    @Test
    void leavesAtDawn() {
        assertTrue(NightShelter.isNight(13000));
        assertTrue(NightShelter.isNight(22900));
        assertFalse(NightShelter.isNight(23100), "dawn: mobs burn, safe to come out");
        assertFalse(NightShelter.isNight(24000 + 1000), "day of the next cycle");
        assertTrue(NightShelter.isNight(24000 + 15000));
    }
}
