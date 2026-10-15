package altoclefplus.build;

import altoclefplus.build.logic.LitematicReader;
import altoclefplus.build.logic.Schematic;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Schematic files the bot can build: built-ins shipped inside the mod ({@code assets/altoclefplus/schematics})
 * and your own in {@code .minecraft/schematics} (the folder Litematica uses). Only {@code .litematic} files.
 */
public final class SchematicLibrary {
    public record Entry(String id, String name) {}

    private static final String BUILTIN_DIR = "assets/altoclefplus/schematics";
    private static final Map<String, Schematic> CACHE = new HashMap<>();

    private SchematicLibrary() {}

    public static Path folder() {
        return FabricLoader.getInstance().getGameDir().resolve("schematics");
    }

    public static List<Entry> list() {
        List<Entry> out = new ArrayList<>();
        builtinDir().ifPresent(dir -> addFiles(dir, "builtin:", out));
        Path folder = folder();
        if (Files.isDirectory(folder)) addFiles(folder, "folder:", out);
        return out;
    }

    /** Loads (and caches) a schematic by id ("builtin:house.litematic" / "folder:house.litematic"). */
    public static Schematic load(String id) throws IOException {
        Schematic cached = CACHE.get(id);
        if (cached != null) return cached;
        Path path = resolve(id);
        if (path == null || !Files.exists(path)) throw new IOException("Schematic not found: " + id);
        try (InputStream in = Files.newInputStream(path)) {
            Schematic s = LitematicReader.read(in);
            CACHE.put(id, s);
            return s;
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static Path resolve(String id) {
        int colon = id.indexOf(':');
        if (colon < 0) return null;
        String file = id.substring(colon + 1);
        if (file.contains("..") || file.contains("/") || file.contains("\\")) return null;
        if (id.startsWith("builtin:")) return builtinDir().map(d -> d.resolve(file)).orElse(null);
        if (id.startsWith("folder:")) return folder().resolve(file);
        return null;
    }

    private static java.util.Optional<Path> builtinDir() {
        return FabricLoader.getInstance().getModContainer("altoclefplus").flatMap(c -> c.findPath(BUILTIN_DIR));
    }

    private static void addFiles(Path dir, String prefix, List<Entry> out) {
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".litematic"))
                    .sorted()
                    .forEach(p -> {
                        String file = p.getFileName().toString();
                        out.add(new Entry(prefix + file, file.substring(0, file.length() - ".litematic".length())));
                    });
        } catch (IOException ignored) {
            // unreadable folder: just nothing listed
        }
    }
}
