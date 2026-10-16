package altoclefplus.core.logic;

/**
 * When the bot holds still for the player: their own inventory is open, or the AltoClef Plus panel is open
 * (the bot kept working and AltoClef closed the panel under the player while crafting).
 */
public final class ScreenPause {
    private ScreenPause() {}

    /**
     * @param inventorySetting "Wait while my inventory is open"
     * @param inventoryOpen    an inventory screen is open
     * @param playersInventory it was opened by the player (not the bot's visible-inventory screen)
     * @param panelOpen        the AltoClef Plus panel (or one of its screens) is open
     * @param panelSetting     "Wait while the panel is open"
     */
    public static boolean shouldPause(boolean inventorySetting, boolean inventoryOpen, boolean playersInventory,
                                      boolean panelOpen, boolean panelSetting) {
        return inventorySetting && inventoryOpen && playersInventory || panelSetting && panelOpen;
    }
}
