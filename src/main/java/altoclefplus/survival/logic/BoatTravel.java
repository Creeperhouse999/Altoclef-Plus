package altoclefplus.survival.logic;

import altoclefplus.camera.logic.CameraSmoother;

import java.util.List;

/** When to cross water by boat, and how to steer one (boats turn with left/right, not the mouse). */
public final class BoatTravel {
    private BoatTravel() {}

    /**
     * @param surfaceAhead      open water surface per block along the straight line toward the goal
     * @param goalDistance      blocks to the goal
     * @param minRun            contiguous water blocks needed (oceans and big lakes, not rivers or ponds)
     * @param maxStartOffset    the water must start within this many blocks of the bot
     * @param minGoalDistance   don't bother for short trips
     */
    public static boolean shouldBoat(List<Boolean> surfaceAhead, double goalDistance, int minRun,
                                     int maxStartOffset, double minGoalDistance) {
        if (goalDistance < minGoalDistance) return false;
        int start = surfaceAhead.indexOf(Boolean.TRUE);
        if (start < 0 || start > maxStartOffset) return false;
        int run = 0;
        for (int i = start; i < surfaceAhead.size() && surfaceAhead.get(i); i++) run++;
        return run >= minRun;
    }

    /** -1 press left, +1 press right, 0 go straight. Boat yaw grows when turning right. */
    public static int turnInput(float boatYaw, float targetYaw, float deadZoneDeg) {
        float diff = CameraSmoother.wrap(targetYaw - boatYaw);
        if (diff > deadZoneDeg) return 1;
        if (diff < -deadZoneDeg) return -1;
        return 0;
    }

    public static boolean shouldStop(boolean landAhead, int waterRemaining) {
        return landAhead || waterRemaining <= 2;
    }
}
