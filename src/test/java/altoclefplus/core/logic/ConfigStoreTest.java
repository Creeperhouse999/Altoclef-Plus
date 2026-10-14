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
