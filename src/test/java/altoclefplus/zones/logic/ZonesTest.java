package altoclefplus.zones.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ZonesTest {
    private static Zone house() {
        return Zone.fromCorners("House", "world1", 10, 60, 10, 0, 70, 0, false);
    }

    @Test
    void cornersInAnyOrderMakeTheSameBox() {
        Zone z = house();
        assertTrue(z.contains(5, 65, 5));
        assertTrue(z.contains(0, 60, 10), "edges are inside");
        assertFalse(z.contains(11, 65, 5));
        assertFalse(z.contains(5, 71, 5), "above the box");
    }

    @Test
    void fullHeightZonesIgnoreY() {
        Zone z = Zone.fromCorners("Zoo", "world1", 0, 64, 0, 5, 64, 5, true);
        assertTrue(z.contains(2, -60, 2));
        assertTrue(z.contains(2, 300, 2));
    }

    @Test
    void newZonesProtectBlocksLootAndFriendlyMobs() {
        ZoneRules r = new ZoneRules();
        assertTrue(r.noBreak && r.noPlace && r.noLoot && r.noAttackFriendly && r.noAttackNeutral);
        assertFalse(r.noEnter);
        assertFalse(r.noAttackHostile);
    }

    @Test
    void zoneSetOnlyUsesZonesOfTheCurrentWorld() {
        Zone other = Zone.fromCorners("Elsewhere", "world2", 0, 0, 0, 100, 100, 100, true);
        ZoneSet set = new ZoneSet(List.of(house(), other), "world1");
        assertTrue(set.forbids(ZoneSet.Action.BREAK, 5, 65, 5));
        assertFalse(set.forbids(ZoneSet.Action.BREAK, 50, 65, 50), "other world's zone ignored");
    }

    @Test
    void rulesCanBeSwitchedPerZone() {
        Zone z = house();
        z.rules.noBreak = false;
        ZoneSet set = new ZoneSet(List.of(z), "world1");
        assertFalse(set.forbids(ZoneSet.Action.BREAK, 5, 65, 5));
        assertTrue(set.forbids(ZoneSet.Action.PLACE, 5, 65, 5));
    }

    @Test
    void attackRulesInsideAndOutsideZones() {
        ZoneSet zones = new ZoneSet(List.of(house()), "world1");
        AttackRules rules = new AttackRules(true, true, Set.of("villager"), true);

        assertTrue(rules.mayAttack(MobCategory.HOSTILE, "zombie", false, false, false, zones, 50, 64, 50));
        assertFalse(rules.mayAttack(MobCategory.FRIENDLY, "cow", true, false, false, zones, 50, 64, 50), "named");
        assertFalse(rules.mayAttack(MobCategory.FRIENDLY, "wolf", false, true, false, zones, 50, 64, 50), "tamed");
        assertFalse(rules.mayAttack(MobCategory.FRIENDLY, "villager", false, false, false, zones, 50, 64, 50), "deny list");
        assertFalse(rules.mayAttack(MobCategory.FRIENDLY, "cow", false, false, false, zones, 5, 65, 5), "petting zoo");
        assertTrue(rules.mayAttack(MobCategory.HOSTILE, "zombie", false, false, false, zones, 5, 65, 5), "hostiles allowed by default");
    }

    @Test
    void selfDefenseOverridesZonesButNotNamedOrTamed() {
        Zone z = house();
        z.rules.noAttackHostile = true;
        ZoneSet zones = new ZoneSet(List.of(z), "world1");
        AttackRules withDefense = new AttackRules(true, true, Set.of(), true);
        AttackRules noDefense = new AttackRules(true, true, Set.of(), false);

        assertTrue(withDefense.mayAttack(MobCategory.HOSTILE, "zombie", false, false, true, zones, 5, 65, 5));
        assertFalse(noDefense.mayAttack(MobCategory.HOSTILE, "zombie", false, false, true, zones, 5, 65, 5));
        assertFalse(withDefense.mayAttack(MobCategory.NEUTRAL, "wolf", false, true, true, zones, 50, 64, 50), "own tamed wolf");
    }

    @Test
    void breakRulesUseZonesAndTheDenyList() {
        ZoneSet zones = new ZoneSet(List.of(house()), "world1");
        BreakRules rules = new BreakRules(Set.of("chest"));
        assertFalse(rules.mayBreak("stone", zones, 5, 65, 5), "inside zone");
        assertFalse(rules.mayBreak("chest", zones, 50, 64, 50), "deny list");
        assertTrue(rules.mayBreak("stone", zones, 50, 64, 50));
    }
}
