package altoclefplus.survival.logic;

import java.util.Optional;

/** Pure math for finding the stronghold from two eye throws and travelling there through the Nether. */
public final class StrongholdTravel {
    private StrongholdTravel() {}

    /**
     * Intersection of two eye-of-ender throw lines on the X/Z plane (height is ignored — eyes rise as they
     * fly, which is what made AltoClef estimate absurd Y values).
     *
     * @return empty when the throws are nearly parallel, the point lies behind either throw, or it's absurdly far
     */
    public static Optional<double[]> intersectXZ(double s1x, double s1z, double d1x, double d1z,
                                                 double s2x, double s2z, double d2x, double d2z,
                                                 double minAngleDeg, double maxDistance) {
        double len1 = Math.hypot(d1x, d1z);
        double len2 = Math.hypot(d2x, d2z);
        if (len1 < 1e-9 || len2 < 1e-9) return Optional.empty();
        double cross = d1x * d2z - d1z * d2x;
        double sinAngle = Math.abs(cross) / (len1 * len2);
        if (sinAngle < Math.sin(Math.toRadians(minAngleDeg))) return Optional.empty();

        double dx = s2x - s1x;
        double dz = s2z - s1z;
        double t1 = (dx * d2z - dz * d2x) / cross;
        double t2 = (dx * d1z - dz * d1x) / cross;
        if (!(t1 > 0) || !(t2 > 0)) return Optional.empty();

        double x = s1x + d1x * t1;
        double z = s1z + d1z * t1;
        if (!Double.isFinite(x) || !Double.isFinite(z)) return Optional.empty();
        if (Math.hypot(x - s2x, z - s2z) > maxDistance) return Optional.empty();
        return Optional.of(new double[]{x, z});
    }

    /** Overworld block coordinate -> Nether block coordinate (floors negatives correctly, unlike x / 8). */
    public static int netherCoord(int overworld) {
        return Math.floorDiv(overworld, 8);
    }

    /** Decide once per trip: Nether only for long trips and only when Nether travel is allowed. */
    public static boolean useNether(double distanceXZ, double minDistance, boolean allowed) {
        return allowed && distanceXZ > minDistance;
    }
}
