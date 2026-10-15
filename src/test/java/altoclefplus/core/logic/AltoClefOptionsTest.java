package altoclefplus.core.logic;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AltoClefOptionsTest {
    /** Stand-in for AltoClef's Settings: private fields of each supported type. */
    static final class FakeSettings {
        private boolean autoEat = true;
        private int minimumFoodAllowed = 0;
        private float resourceMineRange = 100f;
        private double killAnnoyingHostileWhenCloseForSeconds = 12.0;
    }

    @Test
    void readsAndWritesEveryFieldType() {
        FakeSettings s = new FakeSettings();
        assertEquals(1, AltoClefOptions.read(s, "autoEat"));
        AltoClefOptions.write(s, "autoEat", 0);
        assertFalse(s.autoEat);
        AltoClefOptions.write(s, "minimumFoodAllowed", 20);
        assertEquals(20, s.minimumFoodAllowed);
        AltoClefOptions.write(s, "resourceMineRange", 250);
        assertEquals(250f, s.resourceMineRange);
        AltoClefOptions.write(s, "killAnnoyingHostileWhenCloseForSeconds", 4);
        assertEquals(4.0, s.killAnnoyingHostileWhenCloseForSeconds);
        assertTrue(Double.isNaN(AltoClefOptions.read(s, "noSuchField")), "unknown field: no crash");
    }

    @Test
    void appliesOnlyOverridesAndClampsThem() {
        FakeSettings s = new FakeSettings();
        Map<String, Double> overrides = new HashMap<>();
        overrides.put("minimumFoodAllowed", 999.0);
        overrides.put("notAnOption", 5.0);
        AltoClefOptions.applyAll(s, overrides);
        assertEquals(100, s.minimumFoodAllowed, "clamped to the option's range");
        assertTrue(s.autoEat, "no override: AltoClef's own value stays");
    }

    @Test
    void optionsAreUniqueAndSkipOnesOurFeaturesControl() {
        Set<String> seen = new HashSet<>();
        for (AltoClefOptions.Option o : AltoClefOptions.ALL) assertTrue(seen.add(o.field()), o.field());
        for (String ours : new String[]{"autoMLGBucket", "avoidSearchingDungeonChests", "containerItemMoveDelay",
                "netherFastTravelWalkingRange"}) {
            assertFalse(seen.contains(ours), ours + " is set by an AltoClef Plus feature");
        }
    }
}
