package altoclefplus.camera.logic;

/**
 * Exponentially smoothed view angles. Yaw is kept unwrapped so it can follow Minecraft's ever-growing
 * player yaw, but each step takes the short way round.
 */
public final class CameraSmoother {
    private float yaw;
    private float pitch;
    private boolean initialized;

    public void snap(float targetYaw, float targetPitch) {
        yaw = targetYaw;
        pitch = clampPitch(targetPitch);
        initialized = true;
    }

    /**
     * @param k              smoothing rate per second (higher = snappier)
     * @param maxDegPerSec   hard cap on angular speed
     */
    public void update(float targetYaw, float targetPitch, float dtSeconds, float k, float maxDegPerSec) {
        if (!initialized) {
            snap(targetYaw, targetPitch);
            return;
        }
        if (dtSeconds <= 0f) return;
        float alpha = (float) (1.0 - Math.exp(-k * dtSeconds));
        float maxStep = maxDegPerSec * dtSeconds;

        float dYaw = wrap(targetYaw - yaw);
        yaw += clamp(dYaw * alpha, maxStep);
        // Re-anchor near the target so the unwrapped values never drift apart by multiples of 360.
        yaw = targetYaw - wrap(targetYaw - yaw);

        float dPitch = clampPitch(targetPitch) - pitch;
        pitch = clampPitch(pitch + clamp(dPitch * alpha, maxStep));
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    /** Wraps degrees into (-180, 180]. */
    public static float wrap(float deg) {
        float d = deg % 360f;
        if (d <= -180f) d += 360f;
        if (d > 180f) d -= 360f;
        return d;
    }

    private static float clamp(float v, float max) {
        return Math.max(-max, Math.min(max, v));
    }

    private static float clampPitch(float p) {
        return Math.max(-90f, Math.min(90f, p));
    }
}
