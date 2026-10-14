package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatAimTest {
    @Test
    void angleBetweenDirections() {
        assertEquals(0.0, CombatAim.angleTo(1, 0, 0, 5, 0, 0), 1e-6);
        assertEquals(90.0, CombatAim.angleTo(1, 0, 0, 0, 0, 3), 1e-6);
        assertEquals(180.0, CombatAim.angleTo(1, 0, 0, -2, 0, 0), 1e-6);
    }

    @Test
    void zeroLengthDirectionCountsAsFacingAway() {
        assertEquals(180.0, CombatAim.angleTo(0, 0, 0, 1, 0, 0), 1e-6);
    }

    @Test
    void hitsOnlyWithLineOfSightAndFacing() {
        assertTrue(CombatAim.mayHit(true, 10, 30));
        assertTrue(CombatAim.mayHit(true, 30, 30));
        assertFalse(CombatAim.mayHit(true, 31, 30));
        assertFalse(CombatAim.mayHit(true, 180, 30));
        assertFalse(CombatAim.mayHit(false, 0, 30));
    }

    @Test
    void lookAnglesUseMinecraftConventions() {
        float[] south = CombatAim.lookAngles(0, 0, 0, 0, 0, 10);
        assertEquals(0f, south[0], 1e-3);
        assertEquals(0f, south[1], 1e-3);

        float[] west = CombatAim.lookAngles(0, 0, 0, -10, 0, 0);
        assertEquals(90f, west[0], 1e-3);

        float[] up = CombatAim.lookAngles(0, 0, 0, 0, 10, 0.0001);
        assertTrue(up[1] < -89f, "looking up is negative pitch, was " + up[1]);
    }
}
