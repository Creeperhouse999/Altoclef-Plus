package altoclefplus.inventory;

import adris.altoclef.Settings;
import altoclefplus.Plus;
import altoclefplus.inventory.logic.VisibleInventoryPolicy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;

import java.lang.reflect.Field;

/**
 * "Visible inventory": when the bot moves items in its own inventory (2x2 crafting, sorting, equipping) the
 * inventory screen is opened so you can watch the clicks, at a slower, human-like pace, and closed again once
 * the bot is done. Container screens (crafting table, furnace, chests) are already visible.
 */
public final class VisibleInventory {
    /** The exact screen instance we opened, so a screen you open yourself is never mistaken for ours. */
    private static Screen ours;
    private static long lastClickTick;
    private static long tick;
    private static Field delayField;
    private static Float originalDelay;

    private VisibleInventory() {}

    /** Called right before AltoClef clicks a slot. */
    public static void beforeClick(boolean playerInventorySlot) {
        lastClickTick = tick;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity p = client.player;
        if (p == null || Plus.mod() == null) return;
        boolean enabled = Plus.config().modules.inventory && Plus.config().inventory.visibleInventory;
        if (p.isCreative()) return;
        if (!VisibleInventoryPolicy.shouldOpen(enabled, Plus.snapshot().userTaskRunning, playerInventorySlot,
                client.currentScreen != null || p.currentScreenHandler != p.playerScreenHandler,
                VisibleInventoryPolicy.isInventoryJob(Plus.snapshot().taskChain))) return;
        ours = new InventoryScreen(p);
        client.setScreen(ours);
    }

    /** Once per client tick: pace setting, and close our screen when the bot is done with it. */
    public static void tick() {
        tick++;
        if (tick % 20 == 0) applyPace();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity p = client.player;
        if (ours == null) return;
        if (p == null || client.currentScreen != ours) {
            ours = null; // closed by someone else, or replaced
            return;
        }
        if (VisibleInventoryPolicy.shouldClose(tick - lastClickTick, p.currentScreenHandler.getCursorStack().isEmpty(),
                Plus.snapshot().userTaskRunning)) {
            p.closeHandledScreen();
            ours = null;
        }
    }

    /** The open screen is the bot's own inventory screen. */
    public static boolean isOurs() {
        return ours != null && MinecraftClient.getInstance().currentScreen == ours;
    }

    /** Slow AltoClef's item moves down to a watchable pace while visible mode is on (restored when off). */
    private static void applyPace() {
        if (Plus.mod() == null) return;
        Settings settings = Plus.mod().getModSettings();
        if (settings == null) return;
        try {
            if (delayField == null) {
                delayField = Settings.class.getDeclaredField("containerItemMoveDelay");
                delayField.setAccessible(true);
            }
            boolean on = Plus.config().modules.inventory && Plus.config().inventory.visibleInventory;
            float current = delayField.getFloat(settings);
            if (on) {
                if (originalDelay == null) originalDelay = current;
                float want = Math.max(originalDelay, Plus.config().inventory.visibleClickDelay);
                if (current != want) delayField.setFloat(settings, want);
            } else if (originalDelay != null) {
                delayField.setFloat(settings, originalDelay);
                originalDelay = null;
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            delayField = null;
        }
    }
}
