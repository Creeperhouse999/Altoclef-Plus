package altoclefplus.inventory.logic;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Underground, AltoClef only makes planks from logs, so in a mineshaft full of plank blocks it walked off
 * looking for a tree. When its chain is "craft N planks" -> "mine logs", the planks can be mined directly.
 */
public final class PlanksShortcut {
    private static final Pattern CRAFT_PLANKS = Pattern.compile("crafting (\\d+) planks");

    private PlanksShortcut() {}

    /** How many planks AltoClef is trying to make from logs it doesn't have, or 0. */
    public static int planksWanted(List<String> chain) {
        int wanted = 0;
        boolean afterPlanks = false;
        for (String entry : chain) {
            String e = entry.toLowerCase(Locale.ROOT);
            Matcher m = CRAFT_PLANKS.matcher(e);
            if (m.find()) {
                wanted = Integer.parseInt(m.group(1));
                afterPlanks = true;
                continue;
            }
            if (afterPlanks && e.contains("mine and collect") && e.contains("_log")) return wanted;
        }
        return 0;
    }
}
