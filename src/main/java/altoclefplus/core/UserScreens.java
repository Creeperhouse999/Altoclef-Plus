package altoclefplus.core;

import altoclefplus.Plus;
import altoclefplus.inventory.VisibleInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/**
 * Respect the player's own inventory screen: while you have it open the bot waits (no clicks, no walking,
 * nothing gets closed) and carries on as soon as you close it. AltoClef used to close it, or keep mining
 * with it open.
 */
public final class UserScreens {
    private static boolean wasPausing;

    private UserScreens() {}

    /** The open screen is an inventory the player opened (not the bot's visible-inventory screen). */
    public static boolean userInventoryOpen() {
        return MinecraftClient.getInstance().currentScreen instanceof InventoryScreen && !VisibleInventory.isOurs();
    }

    /** The bot should hold still for the player right now. */
    public static boolean pauseBot() {
        return Plus.mod() != null && Plus.config().ui.pauseWhileInventoryOpen && userInventoryOpen();
    }

    /** Once per client tick: keep Baritone paused and let go of keys while the player's inventory is open. */
    public static void tick() {
        boolean pausing = pauseBot();
        if (pausing) {
            var baritone = Plus.mod().getClientBaritone();
            baritone.getPathingBehavior().requestPause();
            if (!wasPausing) baritone.getInputOverrideHandler().clearAllKeys();
        }
        wasPausing = pausing;
    }
}
