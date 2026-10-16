package altoclefplus.inventory.logic;

/**
 * The player's 2x2 crafting grid (window slots 1-4) is only there with no screen or the player's own inventory
 * open. AltoClef counted those slots on any screen, so with a furnace open it read the furnace's output slot as
 * part of the grid, thought it already had the iron ingot, closed the furnace and reopened it forever.
 */
public final class CraftGrid {
    private CraftGrid() {}

    /** @param craftInputSlot the slot is one of the 2x2 grid slots; @param ownScreen no screen or the player inventory */
    public static boolean visible(boolean craftInputSlot, boolean ownScreen) {
        return !craftInputSlot || ownScreen;
    }
}
