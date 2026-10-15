package altoclefplus.camera.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraPolicyTest {
    private static CameraPolicy.Input in(boolean botActive, boolean screen, double moved, boolean dimChanged, boolean combat) {
        return new CameraPolicy.Input(true, botActive, screen, moved, dimChanged, combat, false, true);
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
        CameraPolicy.Input off = new CameraPolicy.Input(false, true, false, 0, false, false, false, true);
        assertEquals(CameraPolicy.Mode.SNAP, CameraPolicy.decide(off, 8f).mode());
    }

    @Test
    void switchingPerspectiveSnaps() {
        CameraPolicy.Input f5 = new CameraPolicy.Input(true, true, false, 0, false, false, true, true);
        assertEquals(CameraPolicy.Mode.SNAP, CameraPolicy.decide(f5, 8f).mode());
    }

    @Test
    void cameraOnAnotherEntityIsNeverSmoothed() {
        CameraPolicy.Input spectating = new CameraPolicy.Input(true, true, false, 0, false, false, false, false);
        assertEquals(CameraPolicy.Mode.SNAP, CameraPolicy.decide(spectating, 8f).mode());
    }

    @Test
    void combatSpeedsUpSmoothing() {
        assertTrue(CameraPolicy.decide(in(true, false, 0, false, true), 8f).combat());
        assertFalse(CameraPolicy.decide(in(true, false, 0, false, false), 8f).combat());
    }

    @Test
    void looksAheadWhileJustWalking() {
        assertEquals(12f, CameraPolicy.viewPitch(80f, true, false, true), 0.01f, "walking, staring at its feet: look ahead");
        assertEquals(5f, CameraPolicy.viewPitch(5f, true, false, true), 0.01f, "already looking ahead: unchanged");
        assertEquals(-20f, CameraPolicy.viewPitch(-60f, true, false, true), 0.01f, "staring at the sky: not that far up either");
        assertEquals(80f, CameraPolicy.viewPitch(80f, true, true, true), 0.01f, "mining/fighting: show what it really looks at");
        assertEquals(80f, CameraPolicy.viewPitch(80f, false, false, true), 0.01f, "standing still: real view");
        assertEquals(80f, CameraPolicy.viewPitch(80f, true, false, false), 0.01f, "setting off");
    }
}
