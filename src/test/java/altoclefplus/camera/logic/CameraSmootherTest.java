package altoclefplus.camera.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraSmootherTest {
    @Test
    void firstUpdateSnapsToTarget() {
        CameraSmoother c = new CameraSmoother();
        c.update(45f, 10f, 0.05f, 10f, 720f);

        assertEquals(45f, c.yaw(), 1e-4);
        assertEquals(10f, c.pitch(), 1e-4);
    }

    @Test
    void movesPartWayThenConverges() {
        CameraSmoother c = new CameraSmoother();
        c.snap(0f, 0f);

        c.update(90f, 0f, 0.05f, 10f, 10_000f);
        assertTrue(c.yaw() > 0f && c.yaw() < 90f, "one frame moves part of the way, was " + c.yaw());

        for (int i = 0; i < 100; i++) c.update(90f, 0f, 0.05f, 10f, 10_000f);
        assertEquals(90f, c.yaw(), 0.5f);
    }

    @Test
    void takesShortWayAcrossTheSeam() {
        CameraSmoother c = new CameraSmoother();
        c.snap(179f, 0f);

        c.update(-179f, 0f, 0.05f, 10f, 10_000f);

        float moved = CameraSmoother.wrap(c.yaw() - 179f);
        assertTrue(moved > 0f && moved <= 2.01f, "should move up to +2 degrees across the seam, moved " + moved);
    }

    @Test
    void followsUnwrappedPlayerYaw() {
        CameraSmoother c = new CameraSmoother();
        c.snap(710f, 0f);

        c.update(730f, 0f, 1f, 1000f, 10_000f);

        assertEquals(730f, c.yaw(), 0.01f);
    }

    @Test
    void stepIsClampedToMaxSpeed() {
        CameraSmoother c = new CameraSmoother();
        c.snap(0f, 0f);

        c.update(170f, 0f, 0.1f, 1000f, 100f);

        assertEquals(10f, c.yaw(), 0.01f);
    }

    @Test
    void zeroTimeMeansNoMovement() {
        CameraSmoother c = new CameraSmoother();
        c.snap(0f, 0f);

        c.update(90f, 30f, 0f, 10f, 720f);

        assertEquals(0f, c.yaw(), 1e-6);
        assertEquals(0f, c.pitch(), 1e-6);
    }

    @Test
    void pitchStaysWithinVerticalLimits() {
        CameraSmoother c = new CameraSmoother();
        c.snap(0f, 80f);

        c.update(0f, 95f, 1f, 1000f, 10_000f);

        assertEquals(90f, c.pitch(), 1e-4);
    }

    @Test
    void wrapNormalisesAngles() {
        assertEquals(-179f, CameraSmoother.wrap(181f), 1e-4);
        assertEquals(10f, CameraSmoother.wrap(370f), 1e-4);
        assertEquals(180f, CameraSmoother.wrap(-180f), 1e-4);
    }
}
