package altoclefplus.core.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigStoreTest {
    @TempDir
    Path dir;

    @Test
    void missingFileGivesDefaultsAndWritesFile() throws Exception {
        Path file = dir.resolve("altoclefplus.json");
        ConfigStore store = new ConfigStore(file);

        PlusConfig cfg = store.load();

        assertEquals(new PlusConfig().camera.smoothness, cfg.camera.smoothness);
        assertTrue(Files.exists(file));
        assertNull(store.lastError());
    }

    @Test
    void invalidJsonGivesDefaultsAndRecordsError() throws Exception {
        Path file = dir.resolve("altoclefplus.json");
        Files.writeString(file, "{ this is not json");
        ConfigStore store = new ConfigStore(file);

        PlusConfig cfg = store.load();

        assertEquals(new PlusConfig().inventory.junkFreeSlotThreshold, cfg.inventory.junkFreeSlotThreshold);
        assertNotNull(store.lastError());
    }

    @Test
    void wrongTypeGivesDefaultsAndRecordsError() throws Exception {
        Path file = dir.resolve("altoclefplus.json");
        Files.writeString(file, "{ \"camera\": { \"smoothness\": \"fast\" } }");
        ConfigStore store = new ConfigStore(file);

        PlusConfig cfg = store.load();

        assertEquals(new PlusConfig().camera.smoothness, cfg.camera.smoothness);
        assertNotNull(store.lastError());
    }

    @Test
    void partialJsonKeepsDefaultsForMissingFields() throws Exception {
        Path file = dir.resolve("altoclefplus.json");
        Files.writeString(file, "{ \"camera\": { \"smoothness\": 4.5 } }");

        PlusConfig cfg = new ConfigStore(file).load();

        assertEquals(4.5f, cfg.camera.smoothness);
        assertEquals(new PlusConfig().camera.combatMultiplier, cfg.camera.combatMultiplier);
        assertEquals(new PlusConfig().recovery.noProgressSeconds, cfg.recovery.noProgressSeconds);
    }

    @Test
    void nullSectionIsReplacedWithDefaults() throws Exception {
        Path file = dir.resolve("altoclefplus.json");
        Files.writeString(file, "{ \"survival\": null, \"inventory\": { \"protectedItems\": null } }");

        PlusConfig cfg = new ConfigStore(file).load();

        assertNotNull(cfg.survival);
        assertFalse(cfg.inventory.protectedItems.isEmpty());
    }

    @Test
    void nullListElementsAndBrokenPresetsAreDropped() throws Exception {
        Path file = dir.resolve("altoclefplus.json");
        Files.writeString(file, "{ \"ui\": { \"hudResourceItems\": [\"food\", null], "
                + "\"presets\": [ {\"name\": null, \"command\": \"get dirt 1\"}, {\"name\": \"Ok\", \"command\": \"food 5\"} ] },"
                + " \"inventory\": { \"junkItems\": [null, \"bone\"] } }");

        PlusConfig cfg = new ConfigStore(file).load();

        assertEquals(java.util.List.of("food"), cfg.ui.hudResourceItems);
        assertEquals(1, cfg.ui.presets.size());
        assertEquals("Ok", cfg.ui.presets.get(0).name);
        assertEquals(java.util.List.of("bone"), cfg.inventory.junkItems);
    }

    @Test
    void numbersAreClampedToSaneRanges() throws Exception {
        Path file = dir.resolve("altoclefplus.json");
        Files.writeString(file, "{ \"camera\": { \"smoothness\": 0, \"maxDegreesPerSecond\": -5 },"
                + " \"inventory\": { \"buildingBlockCapPerType\": 0, \"junkFreeSlotThreshold\": 99 } }");

        PlusConfig cfg = new ConfigStore(file).load();

        assertTrue(cfg.camera.smoothness >= 1);
        assertTrue(cfg.camera.maxDegreesPerSecond >= 30);
        assertTrue(cfg.inventory.buildingBlockCapPerType >= 16);
        assertTrue(cfg.inventory.junkFreeSlotThreshold <= 10);
    }

    @Test
    void saveThenLoadRoundTrips() {
        Path file = dir.resolve("altoclefplus.json");
        ConfigStore store = new ConfigStore(file);
        PlusConfig cfg = new PlusConfig();
        cfg.inventory.craftingTableMaxDistance = 77;
        cfg.modules.camera = false;

        store.save(cfg);
        PlusConfig loaded = store.load();

        assertEquals(77, loaded.inventory.craftingTableMaxDistance);
        assertFalse(loaded.modules.camera);
    }
}
