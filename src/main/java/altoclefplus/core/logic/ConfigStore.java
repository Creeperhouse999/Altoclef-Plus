package altoclefplus.core.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads and saves {@link PlusConfig} as JSON. Never throws: bad files fall back to defaults. */
public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private String lastError;

    public ConfigStore(Path file) {
        this.file = file;
    }

    public PlusConfig load() {
        lastError = null;
        if (!Files.exists(file)) {
            PlusConfig defaults = new PlusConfig();
            defaults.ui.lastSeenVersion = "fresh"; // new install: no "updated" note
            save(defaults);
            return defaults;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            PlusConfig cfg = GSON.fromJson(json, PlusConfig.class);
            return cfg == null ? new PlusConfig() : cfg.normalize();
        } catch (Exception e) {
            lastError = "Invalid config " + file.getFileName() + ": " + e.getMessage();
            return new PlusConfig();
        }
    }

    public void save(PlusConfig cfg) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file, GSON.toJson(cfg), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            lastError = "Could not save config: " + e.getMessage();
        }
    }

    public String lastError() {
        return lastError;
    }
}
