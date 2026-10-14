package altoclefplus.survival.logic;

import altoclefplus.core.logic.PlusConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurvivalLogicTest {
    private final PlusConfig.Survival cfg = new PlusConfig().survival;

    // ---- caution ----
    @Test
    void healthyBotCarriesOn() {
        assertEquals(CautionPolicy.Action.NONE, new CautionPolicy().decide(20, 0, 64, cfg));
    }

    @Test
    void lowHealthWithoutEnemiesHoldsUntilHealed() {
        CautionPolicy p = new CautionPolicy();
        assertEquals(CautionPolicy.Action.HOLD_AND_EAT, p.decide(11, 0, 64, cfg));
        assertEquals(CautionPolicy.Action.HOLD_AND_EAT, p.decide(15, 0, 64, cfg), "keeps holding below resume health");
        assertEquals(CautionPolicy.Action.NONE, p.decide(18, 0, 64, cfg));
    }

    @Test
    void veryLowHealthWithEnemiesFlees() {
        assertEquals(CautionPolicy.Action.FLEE, new CautionPolicy().decide(7, 2, 64, cfg));
    }

    @Test
    void moderateHealthWithEnemiesLetsMobDefenseFight() {
        assertEquals(CautionPolicy.Action.NONE, new CautionPolicy().decide(11, 2, 64, cfg));
    }

    @Test
    void weakBotDoesNotGoDeepUnderground() {
        assertEquals(CautionPolicy.Action.HOLD_AND_EAT, new CautionPolicy().decide(14, 0, 20, cfg));
        assertEquals(CautionPolicy.Action.NONE, new CautionPolicy().decide(14, 0, 64, cfg));
    }

    @Test
    void disabledCautionDoesNothing() {
        cfg.caution = false;
        assertEquals(CautionPolicy.Action.NONE, new CautionPolicy().decide(5, 3, 10, cfg));
    }

    // ---- projectiles ----
    @Test
    void arrowAimedAtPlayerComesClose() {
        ProjectileMath.Approach a = ProjectileMath.closestApproach(0, 1.6, -10, 0, 0.05, 2.5, 0, 1.6, 0, 0.05, 30);
        assertTrue(a.distance() < 1.0, "dist " + a.distance());
        assertTrue(a.ticks() > 0 && a.ticks() < 10);
    }

    @Test
    void arrowFlyingPastStaysFar() {
        ProjectileMath.Approach a = ProjectileMath.closestApproach(5, 1.6, -10, 0, 0, 2.5, 0, 1.6, 0, 0.0, 30);
        assertEquals(5.0, a.distance(), 0.2);
    }

    @Test
    void projectileMovingAwayIsClosestNow() {
        ProjectileMath.Approach a = ProjectileMath.closestApproach(0, 0, 3, 0, 0, 2, 0, 0, 0, 0, 30);
        assertEquals(0, a.ticks());
        assertEquals(3.0, a.distance(), 1e-6);
    }

    @Test
    void strafeGoesToTheSideThePlayerIsAlreadyOn() {
        // projectile flies +z; player slightly to the +x side -> strafe +x
        assertEquals(1, ProjectileMath.strafeSide(0, 1, 0.3, 5));
        assertEquals(-1, ProjectileMath.strafeSide(0, 1, -0.3, 5));
    }

    // ---- death recovery ----
    @Test
    void lavaAndVoidDeathsAreNotRecovered() {
        assertEquals(DeathRecoveryPlanner.Plan.SKIP, DeathRecoveryPlanner.plan("lava", true, 20, 64, cfg));
        assertEquals(DeathRecoveryPlanner.Plan.SKIP, DeathRecoveryPlanner.plan("void", true, 20, 64, cfg));
        assertEquals(DeathRecoveryPlanner.Plan.SKIP, DeathRecoveryPlanner.plan("mob", false, 20, 64, cfg));
    }

    @Test
    void farOrDeepDeathsGetAStarterKitFirst() {
        assertEquals(DeathRecoveryPlanner.Plan.GO, DeathRecoveryPlanner.plan("mob", true, 40, 64, cfg));
        assertEquals(DeathRecoveryPlanner.Plan.KIT_THEN_GO, DeathRecoveryPlanner.plan("mob", true, 400, 64, cfg));
        assertEquals(DeathRecoveryPlanner.Plan.KIT_THEN_GO, DeathRecoveryPlanner.plan("fall", true, 40, 12, cfg));
    }

    @Test
    void itemsExpireAfterSixMinutesOfLoadedTime() {
        assertFalse(DeathRecoveryPlanner.expired(6 * 60 * 20 - 1));
        assertTrue(DeathRecoveryPlanner.expired(6 * 60 * 20));
    }

    @Test
    void deathLoopNeedsDeathsCloseInTimeAndSpace() {
        PlusConfig.Recovery rc = new PlusConfig().recovery;
        DeathLoopGuard g = new DeathLoopGuard();
        g.record(0, 100, 100);
        assertFalse(g.isLoop(rc, 1000));
        g.record(5 * 60_000, 110, 95);
        assertTrue(g.isLoop(rc, 5 * 60_000));
        assertFalse(g.isLoop(rc, 30 * 60_000), "old deaths age out");

        DeathLoopGuard far = new DeathLoopGuard();
        far.record(0, 0, 0);
        far.record(60_000, 500, 500);
        assertFalse(far.isLoop(rc, 60_000));
    }
}
