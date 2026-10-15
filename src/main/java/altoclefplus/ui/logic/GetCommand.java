package altoclefplus.ui.logic;

import java.util.ArrayList;
import java.util.List;

/** Parses simple "get item [count] item [count]..." commands; anything else returns null (not judged). */
public final class GetCommand {
    public record Want(String id, int count) {}

    private GetCommand() {}

    public static List<Want> parse(String command) {
        if (command == null) return null;
        String[] t = command.trim().split("\\s+");
        if (t.length < 2 || !t[0].equalsIgnoreCase("get")) return null;
        List<Want> out = new ArrayList<>();
        for (int i = 1; i < t.length; i++) {
            String id = t[i];
            if (!id.matches("[a-z0-9_]+") || id.matches("\\d+")) return null;
            int count = 1;
            if (i + 1 < t.length && t[i + 1].matches("\\d{1,5}")) {
                count = Integer.parseInt(t[i + 1]);
                i++;
            }
            out.add(new Want(id, count));
        }
        return out;
    }
}
