package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OutdatedToolsTest {
    @Test
    void dropsToolsBeatenByABetterOneOfTheSameKind() {
        var drops = JunkClassifier.outdatedTools(Map.of("wooden_pickaxe", 1, "stone_pickaxe", 1, "wooden_axe", 1,
                "stone_axe", 1, "iron_sword", 1), Set.of(), Set.of());
        assertEquals(List.of(new JunkClassifier.Drop("wooden_axe", 1), new JunkClassifier.Drop("wooden_pickaxe", 1)), drops);
    }

    @Test
    void keepsTheBestAndAnythingNeededOrProtected() {
        assertTrue(JunkClassifier.outdatedTools(Map.of("stone_pickaxe", 2), Set.of(), Set.of()).isEmpty(), "spares of the best tier stay");
        assertTrue(JunkClassifier.outdatedTools(Map.of("wooden_pickaxe", 1, "stone_pickaxe", 1), Set.of("wooden_pickaxe"), Set.of()).isEmpty());
        assertTrue(JunkClassifier.outdatedTools(Map.of("golden_pickaxe", 1, "iron_pickaxe", 1), Set.of(), Set.of("golden_pickaxe")).isEmpty());
        assertTrue(JunkClassifier.outdatedTools(Map.of("golden_sword", 1, "wooden_sword", 1), Set.of(), Set.of()).isEmpty(),
                "best is only wood: nothing is clearly outdated yet");
    }
}
