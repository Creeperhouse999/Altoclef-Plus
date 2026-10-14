package altoclefplus.inventory.logic;

import altoclefplus.core.logic.ItemInfo;

import java.util.List;
import java.util.Set;

/** Which hotbar slot to hold while breaking a block. Never armor, blocks or protected items. */
public final class ToolChoice {
    /** A hotbar slot, its item id ("" for empty) and its mining speed against the block. */
    public record Candidate(int slot, String id, float speed) {}

    private ToolChoice() {}

    /** @return slot to select, or -1 if no safe choice exists (leave the selection alone). */
    public static int pick(List<Candidate> hotbar, Set<String> protectedIds, Set<String> junkIds) {
        int best = -1;
        float bestSpeed = 1.0f;
        for (Candidate c : hotbar) {
            boolean tool = !ItemInfo.toolType(c.id()).isEmpty() || c.id().equals("shears");
            if (tool && c.speed() > bestSpeed) {
                best = c.slot();
                bestSpeed = c.speed();
            }
        }
        if (best >= 0) return best;
        for (Candidate c : hotbar) if (c.id().isEmpty()) return c.slot();
        for (Candidate c : hotbar) {
            if (junkIds.contains(c.id()) && !protectedIds.contains(c.id())) return c.slot();
        }
        return -1;
    }
}
