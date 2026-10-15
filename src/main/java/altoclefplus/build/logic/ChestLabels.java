package altoclefplus.build.logic;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Chest labels: an item frame on a chest showing an item, or a sign with words like "Cobble / Stone", tell
 * the bot what is inside so it goes straight to the right chest. (The frame's own item is never taken.)
 */
public final class ChestLabels {
    /** @param frameItems item ids shown in frames on this chest; @param signText text of signs on it */
    public record Labeled(int chestId, Set<String> frameItems, String signText) {}

    private ChestLabels() {}

    public static Optional<Integer> bestFor(String itemId, List<Labeled> chests) {
        for (Labeled c : chests) if (c.frameItems().contains(itemId)) return Optional.of(c.chestId());
        String name = itemId.substring(itemId.indexOf(':') + 1).toLowerCase(Locale.ROOT); // "cobblestone"
        for (Labeled c : chests) {
            for (String word : c.signText().toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
                if (word.length() < 3) continue;
                // "cobble" matches cobblestone; "stone" matches stone; plural "logs" matches oak_log.
                String w = word.endsWith("s") && word.length() > 3 ? word.substring(0, word.length() - 1) : word;
                if (name.equals(w) || name.startsWith(w) || name.endsWith("_" + w) || name.contains(w + "_")) {
                    return Optional.of(c.chestId());
                }
            }
        }
        return Optional.empty();
    }
}
