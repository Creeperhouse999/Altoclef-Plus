package altoclefplus.recovery.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeathDangerTest {
    @Test
    void pillagersMarkABigAreaAfterOneDeath() {
        assertEquals(48, DeathDanger.avoidRadius(2, false, 32));
    }

    @Test
    void otherwiseOnlyARepeatedDeathMarksTheLoopRadius() {
        assertEquals(32, DeathDanger.avoidRadius(0, true, 32));
        assertEquals(0, DeathDanger.avoidRadius(0, false, 32));
    }
}
