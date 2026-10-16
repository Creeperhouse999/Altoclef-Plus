package altoclefplus.ui.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Checks a "get" command before it runs. AltoClef rejects items outside its catalogue (blocks that need Silk
 * Touch such as grass block, items from other mods such as Create) and then does nothing, while the panel
 * said "Running". Better to say so up front.
 */
public final class GetCheck {
    private GetCheck() {}

    /** @return a message naming the items AltoClef can't get, or null if the command is fine (or not a get) */
    public static String problem(String command, Predicate<String> catalogued) {
        List<Kit.Entry> wanted = Kit.parse(command);
        if (wanted.isEmpty()) return null;
        List<String> missing = new ArrayList<>();
        for (Kit.Entry e : wanted) {
            if (!catalogued.test(e.item())) missing.add(e.item().replace('_', ' '));
        }
        if (missing.isEmpty()) return null;
        return "AltoClef can't get " + String.join(", ", missing)
                + " - not in its item list (Silk Touch blocks and items from other mods are not supported)";
    }
}
