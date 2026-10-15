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
                String item = pretty(t[1]);
                return t.length > 2 && !t[2].equals("1") ? "Get " + item + " x" + t[2] : "Get " + item;
            default:
                return capitalize(command.trim().replace('_', ' '));
        }
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
