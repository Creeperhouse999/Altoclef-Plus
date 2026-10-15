package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreeperChaseTest {
    @Test
    void fightsACreeperThatKeepsFollowing() {
        CreeperChase c = new CreeperChase(80);
        for (int t = 0; t < 80; t++) assertFalse(c.shouldFight(true, 6, true, 20, t), "not yet, tick " + t);
        assertTrue(c.shouldFight(true, 6, true, 20, 80), "followed for 4 s: fight it");
    }

    @Test
    void notWithoutWeaponLowHealthOrWhenItLeaves() {
        CreeperChase c = new CreeperChase(80);
        for (int t = 0; t <= 100; t++) c.shouldFight(true, 6, true, 20, t);
        assertFalse(c.shouldFight(true, 6, false, 20, 101), "no sword/axe");
        assertFalse(c.shouldFight(true, 6, true, 9, 102), "low health: keep running");
        assertFalse(c.shouldFight(false, 0, true, 20, 103), "creeper gone");
        assertFalse(c.shouldFight(true, 6, true, 20, 104), "timer restarted after it left");
    }

    @Test
    void farCreepersDoNotCount() {
        CreeperChase c = new CreeperChase(80);
        for (int t = 0; t <= 200; t++) assertFalse(c.shouldFight(true, 14, true, 20, t));
    }
}
