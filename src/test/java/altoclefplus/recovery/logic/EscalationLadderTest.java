package altoclefplus.recovery.logic;

import altoclefplus.core.logic.PlusConfig;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EscalationLadderTest {
    private final PlusConfig.Recovery cfg = new PlusConfig().recovery;

    @Test
    void stepsUpInOrderRespectingCooldown() {
        EscalationLadder l = new EscalationLadder(cfg);
        assertEquals(Optional.of(EscalationLadder.Level.REPATH), l.onStuck(0));
        assertEquals(Optional.empty(), l.onStuck(5), "cooldown");
        assertEquals(Optional.of(EscalationLadder.Level.BLACKLIST), l.onStuck(10));
        assertEquals(Optional.of(EscalationLadder.Level.RESTART), l.onStuck(20));
        assertEquals(Optional.of(EscalationLadder.Level.ESCAPE), l.onStuck(30));
        assertEquals(Optional.empty(), l.onStuck(45), "cooldown doubled to 20s after a full cycle");
        assertEquals(Optional.of(EscalationLadder.Level.REPATH), l.onStuck(50));
    }

    @Test
    void cooldownIsCapped() {
        EscalationLadder l = new EscalationLadder(cfg);
        double t = 0;
        for (int i = 0; i < 4 * 6; i++) {
            Optional<EscalationLadder.Level> lv = l.onStuck(t);
            t += 1000;
            if (lv.isPresent() && lv.get() == EscalationLadder.Level.SAFE_MODE) break;
        }
        assertTrue(l.currentCooldownSeconds() <= cfg.maxCooldownSeconds);
    }

    @Test
    void entersSafeModeAfterConfiguredCycles() {
        EscalationLadder l = new EscalationLadder(cfg);
        Optional<EscalationLadder.Level> lv = Optional.empty();
        double t = 0;
        for (int i = 0; i < cfg.safeModeAfterCycles * 4; i++) {
            lv = l.onStuck(t);
            t += 1000;
        }
        assertEquals(Optional.of(EscalationLadder.Level.SAFE_MODE), l.onStuck(t));
        assertTrue(l.inSafeMode());
        assertFalse(l.shouldStopBot(), "default never stops the bot");
    }

    @Test
    void canBeConfiguredToStopAfterSafeModes() {
        cfg.stopAfterSafeModeCycles = 1;
        EscalationLadder l = new EscalationLadder(cfg);
        double t = 0;
        for (int i = 0; i < cfg.safeModeAfterCycles * 4 + 1; i++) {
            l.onStuck(t);
            t += 1000;
        }
        assertTrue(l.shouldStopBot());
    }

    @Test
    void resetsAfterCleanProgress() {
        EscalationLadder l = new EscalationLadder(cfg);
        l.onStuck(0);
        l.onStuck(10);
        l.onProgress(10 + cfg.resetAfterCleanSeconds + 1);
        assertEquals(Optional.of(EscalationLadder.Level.REPATH), l.onStuck(200));
    }

    @Test
    void progressSoonAfterStuckDoesNotReset() {
        EscalationLadder l = new EscalationLadder(cfg);
        l.onStuck(0);
        l.onProgress(30);
        assertEquals(Optional.of(EscalationLadder.Level.BLACKLIST), l.onStuck(40));
    }
}
