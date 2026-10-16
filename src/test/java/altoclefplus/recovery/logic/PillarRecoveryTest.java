package altoclefplus.recovery.logic;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class PillarRecoveryTest {
    @Test
    void goesBackDownATowerOnlyUnderOpenSky() {
        assertEquals(OptionalInt.of(70), PillarRecovery.descendTo(90, "overworld", true));
        assertEquals(OptionalInt.empty(), PillarRecovery.descendTo(-14, "overworld", false),
                "underground: digging 20 blocks straight down walked into lava");
        assertEquals(OptionalInt.of(40), PillarRecovery.descendTo(50, "the_nether", true), "never below the Nether's lava sea");
    }
}
