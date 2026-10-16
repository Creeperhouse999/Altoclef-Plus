package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BedChoiceTest {
    @Test
    void carryingABedItSleepsHereInsteadOfWalkingToAFarOne() {
        assertFalse(BedChoice.useKnownBed(370, true), "370 blocks away at night: place our own");
        assertTrue(BedChoice.useKnownBed(20, true), "close by: use it");
        assertTrue(BedChoice.useKnownBed(370, false), "no bed to place: go to the known one");
    }
}
