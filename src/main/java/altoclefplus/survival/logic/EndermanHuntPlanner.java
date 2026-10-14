package altoclefplus.survival.logic;

/**
 * Boat-trap enderman hunting: in a warped forest, place a boat next to the bot, stare at an enderman so it
 * comes over, and once it sits in the boat (it can't teleport out) kill it with critical hits. Falls back to
 * a normal fight when the trap doesn't work or there's no boat.
 */
public final class EndermanHuntPlanner {
    public enum Phase { DONE, COLLECT, GO_TO_NETHER, FIND_FOREST, PLACE_BOAT, AGGRO, WAIT, FIGHT }

    /** How long an angry enderman gets to walk into the boat before we just fight it. */
    public static final double TRAP_PATIENCE_SECONDS = 20;

    public record State(int pearls, int target, boolean inNether, boolean inForest, boolean hasBoatItem,
                        boolean boatPlaced, boolean endermanNearby, boolean seated, boolean angry,
                        double secondsSinceAggro, boolean pearlDropped) {
        public State withPearls(int v) { return new State(v, target, inNether, inForest, hasBoatItem, boatPlaced, endermanNearby, seated, angry, secondsSinceAggro, pearlDropped); }
        public State withPearlDropped(boolean v) { return new State(pearls, target, inNether, inForest, hasBoatItem, boatPlaced, endermanNearby, seated, angry, secondsSinceAggro, v); }
        public State withInNether(boolean v) { return new State(pearls, target, v, inForest, hasBoatItem, boatPlaced, endermanNearby, seated, angry, secondsSinceAggro, pearlDropped); }
        public State withInForest(boolean v) { return new State(pearls, target, inNether, v, hasBoatItem, boatPlaced, endermanNearby, seated, angry, secondsSinceAggro, pearlDropped); }
        public State withBoatItem(boolean v) { return new State(pearls, target, inNether, inForest, v, boatPlaced, endermanNearby, seated, angry, secondsSinceAggro, pearlDropped); }
        public State withBoatPlaced(boolean v) { return new State(pearls, target, inNether, inForest, hasBoatItem, v, endermanNearby, seated, angry, secondsSinceAggro, pearlDropped); }
        public State withEndermanNearby(boolean v) { return new State(pearls, target, inNether, inForest, hasBoatItem, boatPlaced, v, seated, angry, secondsSinceAggro, pearlDropped); }
        public State withSeated(boolean v) { return new State(pearls, target, inNether, inForest, hasBoatItem, boatPlaced, endermanNearby, v, angry, secondsSinceAggro, pearlDropped); }
        public State withAngry(boolean v, double secs) { return new State(pearls, target, inNether, inForest, hasBoatItem, boatPlaced, endermanNearby, seated, v, secs, pearlDropped); }
    }

    private EndermanHuntPlanner() {}

    public static Phase next(State s) {
        if (s.pearls() >= s.target()) return Phase.DONE;
        if (s.pearlDropped()) return Phase.COLLECT;
        if (!s.inNether()) return Phase.GO_TO_NETHER;
        if (s.seated()) return Phase.FIGHT;
        if (s.angry() && s.secondsSinceAggro() > TRAP_PATIENCE_SECONDS) return Phase.FIGHT;
        boolean noTrap = !s.hasBoatItem() && !s.boatPlaced();
        if (noTrap) return s.endermanNearby() ? Phase.FIGHT : Phase.FIND_FOREST;
        if (!s.inForest() && !s.endermanNearby()) return Phase.FIND_FOREST;
        if (!s.boatPlaced()) return Phase.PLACE_BOAT;
        if (s.angry()) return Phase.WAIT;
        return s.endermanNearby() ? Phase.AGGRO : Phase.WAIT;
    }
}
