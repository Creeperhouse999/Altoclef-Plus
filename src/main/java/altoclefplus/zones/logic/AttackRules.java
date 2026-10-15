package altoclefplus.zones.logic;

import java.util.Set;

/**
 * Who the bot may hit. Never named or tamed mobs (by default) or mob types on the deny list; inside zones,
 * the zone's per-category rules. Self-defense (on by default) allows hitting a mob that is attacking the
 * bot even inside a zone - but never named or tamed mobs.
 */
public final class AttackRules {
    private final boolean neverNamed;
    private final boolean neverTamed;
    private final Set<String> denyTypes;
    private final boolean selfDefense;

    public AttackRules(boolean neverNamed, boolean neverTamed, Set<String> denyTypes, boolean selfDefense) {
        this.neverNamed = neverNamed;
        this.neverTamed = neverTamed;
        this.denyTypes = denyTypes;
        this.selfDefense = selfDefense;
    }

    public boolean mayAttack(MobCategory category, String typeId, boolean named, boolean tamed, boolean attackingMe,
                             ZoneSet zones, double x, double y, double z) {
        if (neverNamed && named) return false;
        if (neverTamed && tamed) return false;
        if (attackingMe && selfDefense) return true;
        if (denyTypes.contains(typeId)) return false;
        return !zones.forbidsAttack(category, x, y, z);
    }
}
