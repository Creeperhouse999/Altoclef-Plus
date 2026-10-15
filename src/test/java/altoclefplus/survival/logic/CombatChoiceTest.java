package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static altoclefplus.survival.logic.CritGate.Decision.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatChoiceTest {
    private static final Map<String, Integer> KIT = Map.of("iron_sword", 1, "iron_axe", 1, "cobblestone", 30);

    @Test
    void swordForGroupsAxeForShieldsHighestDpsOtherwise() {
        assertEquals("iron_sword", CritTiming.chooseWeapon(KIT, 3, false, 20), "sweep a group");
        assertEquals("iron_axe", CritTiming.chooseWeapon(KIT, 1, true, 20), "axe disables shields");
        assertEquals("iron_sword", CritTiming.chooseWeapon(KIT, 1, false, 20), "sword: more damage per second");
        assertEquals("", CritTiming.chooseWeapon(Map.of("dirt", 1), 1, false, 20));
    }

    @Test
    void axeWhenOneBigHitFinishesTheTarget() {
        // 8 hp left: an iron axe crit (13.5) kills, an iron sword crit (9) also kills -> sword (faster).
        assertEquals("iron_sword", CritTiming.chooseWeapon(KIT, 1, false, 8));
        // 12 hp: only the axe kills in one hit.
        assertEquals("iron_axe", CritTiming.chooseWeapon(KIT, 1, false, 12));
    }

    @Test
    void critOnlyWhenSafe() {
        assertTrue(CritTiming.canCrit(false, false, false, false, false, true));
        assertFalse(CritTiming.canCrit(true, false, false, false, false, true), "in water");
        assertFalse(CritTiming.canCrit(false, false, true, false, false, true), "on a ladder");
        assertFalse(CritTiming.canCrit(false, false, false, false, false, false), "no headroom");
    }

    @Test
    void gateJumpsWaitsForFallThenHits() {
        CritGate g = new CritGate(15);
        assertEquals(WAIT, g.decide(0.5f, true, 0, true, 0), "charge bar not full");
        assertEquals(JUMP, g.decide(1f, true, 0, true, 1));
        assertEquals(WAIT, g.decide(1f, false, 0.3, true, 2), "still going up");
        assertEquals(HIT, g.decide(1f, false, -0.2, true, 6), "falling: crit");
        assertEquals(JUMP, g.decide(1f, true, 0, true, 7), "next hit starts a new jump");
    }

    @Test
    void gateHitsNormallyWhenUnsafeOrStuck() {
        CritGate g = new CritGate(15);
        assertEquals(HIT, g.decide(1f, true, 0, false, 0), "unsafe: plain hit");
        assertEquals(JUMP, g.decide(1f, true, 0, true, 1));
        assertEquals(WAIT, g.decide(1f, true, 0, true, 5), "jump didn't happen yet");
        assertEquals(HIT, g.decide(1f, true, 0, true, 17), "timeout: never stall the fight");
    }
}
