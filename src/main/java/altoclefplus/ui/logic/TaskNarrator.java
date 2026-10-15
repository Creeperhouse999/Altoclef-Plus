package altoclefplus.ui.logic;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns AltoClef's task chain into a short, human HUD line. AltoClef's root task often has a misleading
 * name (a "get iron_pickaxe" run is a crafting-table task for minutes while it mines iron), so the line is
 * built from the goal (the command you ran) and the deepest step that says what is happening right now.
 */
public final class TaskNarrator {
    private static final Pattern CRAFT = Pattern.compile("craft ([a-z0-9_]+)\\}");
    private static final Pattern COLLECT = Pattern.compile("mine and collect: \\[\\[([a-z0-9_]+)");
    private static final Pattern KILLING = Pattern.compile("killing entity\\.minecraft\\.([a-z_]+)");
    /** Animals AltoClef kills for food, wool or leather. */
    private static final java.util.Set<String> PASSIVE = java.util.Set.of("sheep", "cow", "pig", "chicken", "rabbit",
            "mooshroom", "horse", "salmon", "cod", "squid", "goat", "hoglin");
    private static final Pattern DIMENSION = Pattern.compile("going to dimension: ([a-z_]+)");
    private static final Pattern EAT = Pattern.compile("\\beat(ing)?\\b");
    private static final Pattern SLEEP = Pattern.compile("\\bsleep|\\bbeds?\\b");
    private static final Pattern CHEST = Pattern.compile("\\bloot|\\bchests?\\b");

    private TaskNarrator() {}

    /** "get iron_boots 1" -> "Get iron boots"; "get spruce_log 40" -> "Get spruce log x40". */
    public static String goal(String command) {
        if (command == null || command.isBlank()) return "";
        String[] t = command.trim().split("\\s+");
        String head = t[0].toLowerCase(Locale.ROOT);
        switch (head) {
            case "gamer":
                return "Beat the game";
            case "food":
                return t.length > 1 ? "Food (" + t[1] + ")" : "Food";
            case "get":
                if (t.length < 2) return "Get";
                List<GetCommand.Want> wants = GetCommand.parse(command);
                if (wants != null && wants.size() > 1) {
                    StringBuilder sb = new StringBuilder("Get ");
                    for (int i = 0; i < wants.size(); i++) sb.append(i > 0 ? ", " : "").append(pretty(wants.get(i).id()));
                    return sb.toString();
                }
                String item = pretty(t[1]);
                return t.length > 2 && !t[2].equals("1") ? "Get " + item + " x" + t[2] : "Get " + item;
            default:
                return capitalize(command.trim().replace('_', ' '));
        }
    }

    /** {@link #goal} plus "(have/want)" for a single-item get command. */
    public static String goalWithProgress(String command, java.util.function.ToIntFunction<String> have) {
        String g = goal(command);
        List<GetCommand.Want> wants = GetCommand.parse(command);
        if (wants == null || wants.size() != 1) return g;
        GetCommand.Want w = wants.get(0);
        int n = have.applyAsInt(w.id());
        if (n < 0) return g; // an AltoClef group name like "log" or "planks": nothing to count
        return g + " (" + Math.min(w.count(), n) + "/" + w.count() + ")";
    }

    /**
     * What the bot is doing now, from the deepest step that means something.
     * {@code minedBlock} is the block id at the "Destroy block" position when known, else "".
     */
    public static String action(List<String> chain, String minedBlock) {
        for (int i = chain.size() - 1; i >= 0; i--) {
            String said = narrate(chain.get(i).toLowerCase(Locale.ROOT), minedBlock);
            if (said != null && !said.isEmpty()) return said;
        }
        for (int i = chain.size() - 1; i >= 0; i--) {
            String name = cleanName(chain.get(i));
            if (!name.isEmpty()) return name;
        }
        return "";
    }

    /** null = no opinion (look further up); "" = generic step, skip. */
    private static String narrate(String e, String minedBlock) {
        if (e.contains("destroy block")) return minedBlock.isEmpty() ? "" : "Mining " + pretty(minedBlock);
        Matcher m = CRAFT.matcher(e);
        if (m.find()) return "Crafting " + pretty(m.group(1));
        m = COLLECT.matcher(e);
        if (m.find()) return "Collecting " + pretty(m.group(1));
        if (e.contains("furnace") || e.contains("smelt")) return "Smelting";
        if (e.contains("creeper")) return "Running from a creeper";
        if (e.contains("nigerundayo") || e.contains("run away") || e.contains("runaway")) return "Running from mobs";
        m = KILLING.matcher(e);
        if (m.find()) {
            String mob = m.group(1);
            return (PASSIVE.contains(mob) ? "Hunting " : "Fighting ") + pretty(mob);
        }
        if (e.contains("kill")) return "Fighting mobs";
        if (e.contains("pickup dropped") || e.contains("approach entity entity.minecraft.item")) return "Picking up items";
        if (e.contains("wander")) return "Exploring";
        if (e.contains("equipping armor")) return "Putting on armor";
        if (EAT.matcher(e).find()) return "Eating";
        if (SLEEP.matcher(e).find()) return "Sleeping";
        if (CHEST.matcher(e).find()) return "Looting a chest";
        m = DIMENSION.matcher(e);
        if (m.find()) return "Going to the " + pretty(m.group(1));
        if (e.contains("crafting_table")) return "Crafting";
        if (e.contains("interact using") || e.contains("mining or collecting") || e.contains("doing something to closest")
                || e.contains("clicking") || e.contains("receiving output")) return "";
        return null;
    }

    /** "<Some new thing: details> debug" -> "Some new thing". */
    static String cleanName(String entry) {
        int start = entry.indexOf('<');
        int end = entry.indexOf('>');
        String name = start >= 0 && end > start ? entry.substring(start + 1, end) : entry;
        int colon = name.indexOf(':');
        if (colon > 0) name = name.substring(0, colon);
        return name.trim();
    }

    static String pretty(String id) {
        return id.replace('_', ' ');
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
