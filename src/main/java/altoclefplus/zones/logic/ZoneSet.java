package altoclefplus.zones.logic;

import java.util.ArrayList;
import java.util.List;

/** The zones of the current world, answering "may the bot do X here?". */
public final class ZoneSet {
    public enum Action { BREAK, PLACE, LOOT, ENTER }

    private final List<Zone> zones;

    public ZoneSet(List<Zone> all, String world) {
        List<Zone> mine = new ArrayList<>();
        for (Zone z : all) if (z != null && world.equals(z.world)) mine.add(z);
        this.zones = mine;
    }

    public static ZoneSet empty() {
        return new ZoneSet(List.of(), "");
    }

    public boolean forbids(Action action, int x, int y, int z) {
        for (Zone zone : zones) {
            if (!zone.contains(x, y, z)) continue;
            ZoneRules r = zone.rules();
            boolean no = switch (action) {
                case BREAK -> r.noBreak;
                case PLACE -> r.noPlace;
                case LOOT -> r.noLoot;
                case ENTER -> r.noEnter;
            };
            if (no) return true;
        }
        return false;
    }

    /** Is attacking this kind of mob forbidden at this spot? */
    public boolean forbidsAttack(MobCategory category, double x, double y, double z) {
        for (Zone zone : zones) {
            if (!zone.contains(x, y, z)) continue;
            ZoneRules r = zone.rules();
            boolean no = switch (category) {
                case HOSTILE -> r.noAttackHostile;
                case NEUTRAL -> r.noAttackNeutral;
                case FRIENDLY -> r.noAttackFriendly;
            };
            if (no) return true;
        }
        return false;
    }

    public List<Zone> zones() {
        return zones;
    }
}
