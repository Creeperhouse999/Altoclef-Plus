package altoclefplus.survival.logic;

/** Predicts where a projectile goes (Minecraft drag 0.99/tick plus gravity) and which way to sidestep. */
public final class ProjectileMath {
    public record Approach(double distance, int ticks) {}

    private ProjectileMath() {}

    /**
     * Closest distance between a projectile and a fixed target point over the next {@code horizonTicks}.
     *
     * @param gravity downward acceleration per tick (arrows 0.05, fireballs 0)
     */
    public static Approach closestApproach(double px, double py, double pz, double vx, double vy, double vz,
                                           double tx, double ty, double tz, double gravity, int horizonTicks) {
        double best = Double.MAX_VALUE;
        int bestTick = 0;
        for (int t = 0; t <= horizonTicks; t++) {
            double dx = px - tx;
            double dy = py - ty;
            double dz = pz - tz;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d < best) {
                best = d;
                bestTick = t;
            }
            px += vx;
            py += vy;
            pz += vz;
            vx *= 0.99;
            vy = vy * 0.99 - gravity;
            vz *= 0.99;
        }
        return new Approach(best, bestTick);
    }

    /**
     * Side to step to, perpendicular to the projectile's horizontal path: +1 along (vz, -vx), -1 the other way.
     * Picks the side the player is already offset to, so the step takes it further out of the line.
     *
     * @param relX player x minus projectile x
     * @param relZ player z minus projectile z
     */
    public static int strafeSide(double vx, double vz, double relX, double relZ) {
        double dot = relX * vz - relZ * vx;
        return dot >= 0 ? 1 : -1;
    }
}
