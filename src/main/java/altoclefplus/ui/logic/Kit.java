package altoclefplus.ui.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A kit: a list of items with amounts, run as one AltoClef command ("get [dirt 64, bread 16, iron_sword 1]")
 * so the bot gathers everything in one go.
 */
public final class Kit {
    public record Entry(String item, int count) {}

    private Kit() {}

    public static String command(List<Entry> entries) {
        if (entries.isEmpty()) return "";
        if (entries.size() == 1) return "get " + entries.get(0).item() + " " + entries.get(0).count();
        List<String> parts = new ArrayList<>();
        for (Entry e : entries) parts.add(e.item() + " " + e.count());
        return "get [" + String.join(", ", parts) + "]";
    }

    /** Adds an amount; the same item again adds up. */
    public static List<Entry> add(List<Entry> entries, String item, int count) {
        List<Entry> out = new ArrayList<>();
        boolean merged = false;
        for (Entry e : entries) {
            if (e.item().equals(item)) {
                out.add(new Entry(item, e.count() + count));
                merged = true;
            } else {
                out.add(e);
            }
        }
        if (!merged && count > 0) out.add(new Entry(item, count));
        return out;
    }

    /** Takes an amount away; the entry disappears at zero. */
    public static List<Entry> remove(List<Entry> entries, String item, int count) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : entries) {
            if (!e.item().equals(item)) {
                out.add(e);
            } else if (e.count() > count) {
                out.add(new Entry(item, e.count() - count));
            }
        }
        return out;
    }

    /** The entries of a "get" command, or empty if it is not one. */
    public static List<Entry> parse(String command) {
        String c = command.trim();
        if (c.startsWith("@")) c = c.substring(1);
        if (!c.toLowerCase(Locale.ROOT).startsWith("get ")) return List.of();
        String rest = c.substring(4).trim();
        List<Entry> out = new ArrayList<>();
        if (rest.startsWith("[") && rest.endsWith("]")) {
            for (String part : rest.substring(1, rest.length() - 1).split(",")) {
                Entry e = entry(part.trim());
                if (e != null) out = add(out, e.item(), e.count());
            }
        } else {
            Entry e = entry(rest);
            if (e != null) out.add(e);
        }
        return out;
    }

    private static Entry entry(String part) {
        String[] p = part.split("\\s+");
        if (p.length == 0 || p[0].isEmpty() || p.length > 2) return null;
        int n = 1;
        if (p.length == 2) {
            try {
                n = Integer.parseInt(p[1]);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return n > 0 ? new Entry(p[0], n) : null;
    }
}
