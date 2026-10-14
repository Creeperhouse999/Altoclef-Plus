package altoclefplus.survival.logic;

import altoclefplus.core.logic.ItemInfo;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Optional safety steps before risky trips: a bed (respawn point) by the Nether portal, a chest for
 * valuables, and a backup set of gear before the Nether or the End. Each runs once per run.
 */
public final class StashPlanner {
    public enum Job { BED_AT_PORTAL, VALUABLES, BACKUP_BEFORE_NETHER, BACKUP_BEFORE_END }

    /** Items worth leaving behind in a chest before the Nether (not needed there). */
    public static final List<String> VALUABLES = List.of("diamond", "emerald", "lapis_lazuli", "redstone");

    private final Set<Job> done = EnumSet.noneOf(Job.class);

    public Optional<Job> next(Snapshot s, PlusConfig.Survival cfg, boolean portalNearby) {
        if (GatePlanner.headingToNether(s)) {
            if (cfg.bedNearPortal && !done.contains(Job.BED_AT_PORTAL) && portalNearby
                    && ItemInfo.countMatching(s, ItemInfo::isBed) > 0) {
                return Optional.of(Job.BED_AT_PORTAL);
            }
            if (cfg.stashValuablesBeforeNether && !done.contains(Job.VALUABLES)
                    && VALUABLES.stream().anyMatch(s::has)) {
                return Optional.of(Job.VALUABLES);
            }
            if (cfg.backupGearBeforeNether && !done.contains(Job.BACKUP_BEFORE_NETHER)) {
                return Optional.of(Job.BACKUP_BEFORE_NETHER);
            }
        }
        if (cfg.backupGearBeforeEnd && !done.contains(Job.BACKUP_BEFORE_END) && headingToEnd(s)) {
            return Optional.of(Job.BACKUP_BEFORE_END);
        }
        return Optional.empty();
    }

    public void markDone(Job job) {
        done.add(job);
    }

    public void reset() {
        done.clear();
    }

    static boolean headingToEnd(Snapshot s) {
        if (!"overworld".equals(s.dimension)) return false;
        for (String t : s.taskChain) {
            String l = t.toLowerCase(Locale.ROOT);
            if (l.contains("opening end portal") || l.contains("getting equipment for end")) return true;
        }
        return false;
    }
}
