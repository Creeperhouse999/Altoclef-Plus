package altoclefplus.ui.logic;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KitAndPresetsTest {
    @Test
    void aKitIsOneGetCommandForEverything() {
        List<Kit.Entry> kit = List.of(new Kit.Entry("dirt", 64), new Kit.Entry("bread", 16), new Kit.Entry("iron_sword", 1));
        assertEquals("get [dirt 64, bread 16, iron_sword 1]", Kit.command(kit));
        assertEquals("get golden_helmet 1", Kit.command(List.of(new Kit.Entry("golden_helmet", 1))));
        assertEquals("", Kit.command(List.of()));
    }

    @Test
    void addingTheSameItemAgainAddsUpAndCommandsParseBack() {
        List<Kit.Entry> kit = Kit.add(Kit.add(List.of(), "bread", 8), "bread", 8);
        assertEquals(List.of(new Kit.Entry("bread", 16)), kit);
        assertEquals(List.of(new Kit.Entry("dirt", 64), new Kit.Entry("bread", 16)), Kit.parse("get [dirt 64, bread 16]"));
        assertEquals(List.of(new Kit.Entry("stone_axe", 1)), Kit.parse("get stone_axe"));
        assertEquals(List.of(), Kit.parse("gamer"), "not a get command");
        assertEquals(List.of(new Kit.Entry("bread", 6)), Kit.remove(List.of(new Kit.Entry("bread", 16)), "bread", 10));
        assertEquals(List.of(), Kit.remove(List.of(new Kit.Entry("bread", 16)), "bread", 99));
    }

    @Test
    void libraryHasManyPresetsWithUniqueNamesAndValidCommands() {
        Set<String> names = new HashSet<>();
        int count = 0;
        for (PresetLibrary.Category c : PresetLibrary.all()) {
            assertFalse(c.presets().isEmpty(), c.name());
            for (PresetLibrary.Preset p : c.presets()) {
                assertTrue(names.add(p.name()), "duplicate name " + p.name());
                assertFalse(p.command().isBlank());
                if (p.command().startsWith("get ")) assertFalse(Kit.parse(p.command()).isEmpty(), p.name());
                count++;
            }
        }
        assertTrue(count >= 30, "many presets");
        assertTrue(names.contains("Wood tools + leather armor"), "the user's example kit");
    }
}
