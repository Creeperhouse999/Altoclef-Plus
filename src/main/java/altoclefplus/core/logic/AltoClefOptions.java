package altoclefplus.core.logic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * AltoClef's own options shown in our Settings. Changes are kept as overrides in our config and pushed onto
 * AltoClef's settings object in memory (its settings file is never written). Options that an AltoClef Plus
 * feature already controls (water clutch, chest looting, click pace, Nether travel) are not listed.
 */
public final class AltoClefOptions {
    public enum Kind { TOGGLE, NUMBER }

    public record Option(String field, String label, Kind kind, int min, int max, int step) {
        static Option toggle(String field, String label) {
            return new Option(field, label, Kind.TOGGLE, 0, 1, 1);
        }

        static Option number(String field, String label, int min, int max, int step) {
            return new Option(field, label, Kind.NUMBER, min, max, step);
        }
    }

    public static final List<Option> ALL = List.of(
            Option.toggle("autoEat", "Eat automatically"),
            Option.toggle("autoRespawn", "Respawn automatically after dying"),
            Option.toggle("autoReconnect", "Reconnect after a disconnect"),
            Option.toggle("mobDefense", "Fight back / run from mobs"),
            Option.toggle("killOrAvoidAnnoyingHostiles", "Deal with mobs that keep following"),
            Option.toggle("extinguishSelfWithWater", "Put out fire with water"),
            Option.toggle("replantCrops", "Replant crops after harvesting"),
            Option.toggle("collectPickaxeFirst", "Get a pickaxe first on new tasks"),
            Option.toggle("avoidOceanBlocks", "Avoid oceans when searching"),
            Option.toggle("throwAwayUnusedItems", "Throw away unused items when full"),
            Option.toggle("useCraftingBookToCraft", "Craft with the recipe book (faster)"),
            Option.toggle("showTimer", "Show AltoClef's task timer"),
            Option.number("minimumFoodAllowed", "Keep at least this much food", 0, 100, 5),
            Option.number("foodUnitsToCollect", "Food to collect when low", 0, 200, 10),
            Option.number("resourceMineRange", "Max distance to mine resources", 25, 500, 25),
            Option.number("resourceChestLocateRange", "Max distance to chests for resources", 0, 1000, 50),
            Option.number("killAnnoyingHostileWhenCloseForSeconds", "Fight a following mob after (s)", 0, 60, 2),
            Option.number("reservedBuildingBlockCount", "Building blocks to keep", 0, 256, 16));

    private AltoClefOptions() {}

    public static Option find(String field) {
        for (Option o : ALL) if (o.field().equals(field)) return o;
        return null;
    }

    /** Current value (toggles as 0/1), or NaN if the field doesn't exist in this AltoClef version. */
    public static double read(Object settings, String field) {
        try {
            Field f = field(settings, field);
            Class<?> t = f.getType();
            if (t == boolean.class) return f.getBoolean(settings) ? 1 : 0;
            if (t == int.class) return f.getInt(settings);
            if (t == float.class) return f.getFloat(settings);
            if (t == double.class) return f.getDouble(settings);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        return Double.NaN;
    }

    public static void write(Object settings, String field, double value) {
        try {
            Field f = field(settings, field);
            Class<?> t = f.getType();
            if (t == boolean.class) f.setBoolean(settings, value >= 0.5);
            else if (t == int.class) f.setInt(settings, (int) Math.round(value));
            else if (t == float.class) f.setFloat(settings, (float) value);
            else if (t == double.class) f.setDouble(settings, value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // field missing in this AltoClef version: nothing to do
        }
    }

    /** Push every override that belongs to a listed option, clamped to its range. */
    public static void applyAll(Object settings, Map<String, Double> overrides) {
        if (settings == null || overrides == null) return;
        for (Map.Entry<String, Double> e : overrides.entrySet()) {
            Option o = find(e.getKey());
            if (o == null || e.getValue() == null) continue;
            double v = Math.max(o.min(), Math.min(o.max(), e.getValue()));
            if (read(settings, o.field()) != v) write(settings, o.field(), v);
        }
    }

    private static Field field(Object settings, String name) throws NoSuchFieldException {
        Field f = settings.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }
}
