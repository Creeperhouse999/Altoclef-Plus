package altoclefplus.camera.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraPolicyTest {
    private static CameraPolicy.Input in(boolean botActive, boolean screen, double moved, boolean dimChanged, boolean combat) {
        return new CameraPolicy.Input(true, botActive, screen, moved, dimChanged, combat);
    }

    @Test
    void smoothsWhileBotIsActive() {
        assertEquals(CameraPolicy.Mode.SMOOTH, CameraPolicy.decide(in(true, false, 0.2, false, false), 8f).mode());
    }

    @Test
    void manualControlIsNeverSmoothed() {
        assertEquals(CameraPolicy.Mode.SNAP, CameraPolicy.decide(in(false, false, 0.2, false, false), 8f).mode());
    }

    @Test
    void openScreenSnaps() {
        assertEquals(CameraPolicy.Mode.SNAP, CameraPolicy.decide(in(true, true, 0, false, false), 8f).mode());
    }

    @Test
    void teleportAndDimensionChangeSnap() {
        assertEquals(CameraPolicy.Mode.SNAP, CameraPolicy.decide(in(true, false, 20, false, false), 8f).mode());
        assertEquals(CameraPolicy.Mode.SNAP, CameraPolicy.decide(in(true, false, 0, true, false), 8f).mode());
    }

    @Test
    void disabledModuleSnaps() {
        CameraPolicy.Input off = new CameraPolicy.Input(false, true, false, 0, false, false);
        assertEquals(CameraPolicy.Mode.SNAP, CameraPolicy.decide(off, 8f).mode());
    }

    @Test
    void combatSpeedsUpSmoothing() {
        assertTrue(CameraPolicy.decide(in(true, false, 0, false, true), 8f).combat());
        assertFalse(CameraPolicy.decide(in(true, false, 0, false, false), 8f).combat());
    }
}
