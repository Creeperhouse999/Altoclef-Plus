package altoclefplus.core.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenPauseTest {
    @Test
    void theBotWaitsWhileThePanelIsOpenIfThatSettingIsOn() {
        assertTrue(ScreenPause.shouldPause(true, false, false, true, true), "panel open, setting on");
        assertFalse(ScreenPause.shouldPause(true, false, false, true, false), "panel open, setting off");
        assertTrue(ScreenPause.shouldPause(true, true, true, false, false), "own inventory open");
        assertFalse(ScreenPause.shouldPause(false, true, true, false, false), "inventory setting off");
        assertFalse(ScreenPause.shouldPause(true, true, false, false, false), "the bot's own visible inventory");
    }
}
