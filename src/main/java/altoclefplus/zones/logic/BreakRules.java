package altoclefplus.zones.logic;

import java.util.Set;

/** Which blocks the bot may break: never inside no-break zones, never block types on the deny list. */
public final class BreakRules {
    private final Set<String> denyBlocks;

    public BreakRules(Set<String> denyBlocks) {
        this.denyBlocks = denyBlocks;
    }

    public boolean mayBreak(String blockId, ZoneSet zones, int x, int y, int z) {
        if (denyBlocks.contains(blockId)) return false;
        return !zones.forbids(ZoneSet.Action.BREAK, x, y, z);
    }
}
