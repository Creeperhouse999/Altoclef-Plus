package altoclefplus.survival.logic;

/** Pure aiming math for "fair combat": only hit what the bot can see and is facing. */
public final class CombatAim {
    private CombatAim() {}

    /** Angle in degrees between the look vector and the direction to the target. */
    public static double angleTo(double lx, double ly, double lz, double dx, double dy, double dz) {
        double ll = Math.sqrt(lx * lx + ly * ly + lz * lz);
        double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (ll < 1e-9 || dl < 1e-9) return 180.0;
        double cos = (lx * dx + ly * dy + lz * dz) / (ll * dl);
        return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, cos))));
    }

    public static boolean mayHit(boolean lineOfSight, double angleDeg, double maxAngleDeg) {
        return lineOfSight && angleDeg <= maxAngleDeg;
    }

    /** Minecraft yaw/pitch (yaw 0 = +Z/south, 90 = -X/west; negative pitch = up) from one point to another. */
    public static float[] lookAngles(double fx, double fy, double fz, double tx, double ty, double tz) {
        double dx = tx - fx;
        double dy = ty - fy;
        double dz = tz - fz;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horiz));
        return new float[]{yaw, pitch};
    }
}
