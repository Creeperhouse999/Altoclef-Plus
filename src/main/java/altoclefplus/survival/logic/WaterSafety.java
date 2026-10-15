package altoclefplus.survival.logic;

/** Drowning rescue and cave-water avoidance decisions. */
public final class WaterSafety {
    public enum AirAction { NONE, SWIM_UP, DOOR_POCKET }

    /** Air is 300 when full; drowning damage starts below 0. Act with a safety margin. */
    public static final int LOW_AIR = 120;
    /** Rough ticks of air spent per block of swimming straight up, plus a margin. */
    private static final int TICKS_PER_BLOCK_UP = 8;
    private static final int MARGIN = 20;

    private WaterSafety() {}

    /**
     * @param waterAbove water blocks between the head and open air straight above, or -1 if a solid block
     *                   is in the way (cave ceiling)
     */
    public static AirAction airAction(int air, boolean headUnderwater, int waterAbove, boolean hasDoor) {
        if (!headUnderwater || air >= LOW_AIR) return AirAction.NONE;
        boolean surfaceReachable = waterAbove >= 0 && waterAbove * TICKS_PER_BLOCK_UP + MARGIN < air;
        if (surfaceReachable) return AirAction.SWIM_UP;
        if (hasDoor) return AirAction.DOOR_POCKET;
        // No door: swimming up only helps if there is open water above at all; under a ceiling, don't take
        // over (cancelling the path would trap the bot) and let pathing find the way out.
        return waterAbove >= 0 ? AirAction.SWIM_UP : AirAction.NONE;
    }

    /** Extra path cost for a move ending in water underground (no penalty for open-sky lakes and oceans). */
    public static double extraCost(boolean feetInWater, boolean headInWater, boolean underground, double penalty) {
        if (!feetInWater || !underground) return 0;
        return headInWater ? penalty * 2.5 : penalty;
    }
}
