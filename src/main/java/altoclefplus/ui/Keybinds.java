package altoclefplus.ui;

import altoclefplus.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/** Our three keys. Registered into vanilla Controls by GameOptionsMixin (no Fabric API needed). */
public final class Keybinds {
    public static final String CATEGORY = "key.categories.altoclefplus";

    public static final KeyBinding PANEL = new KeyBinding("key.altoclefplus.panel", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY);
    public static final KeyBinding STOP = new KeyBinding("key.altoclefplus.stop", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    // O, not P: P is vanilla's Social Interactions key.
    public static final KeyBinding PAUSE = new KeyBinding("key.altoclefplus.pause", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, CATEGORY);

    static {
        Map<String, Integer> order = KeyBindingAccessor.altoclefplus$getCategoryOrderMap();
        if (!order.containsKey(CATEGORY)) {
            int max = order.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            order.put(CATEGORY, max + 1);
        }
    }

    private Keybinds() {}

    /** Panel / Stop / Pause keys. Polled every tick even if the UI module is off or failed. */
    public static void poll() {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        while (PANEL.wasPressed()) {
            if (client.currentScreen == null) client.setScreen(new PanelScreen());
        }
        while (STOP.wasPressed()) {
            altoclefplus.Plus.control().stop();
            alert("Stopped");
        }
        while (PAUSE.wasPressed()) {
            altoclefplus.Plus.control().togglePause();
            alert(altoclefplus.Plus.control().state().label);
        }
    }

    private static void alert(String text) {
        if (altoclefplus.Plus.ui() != null) altoclefplus.Plus.ui().alert(text);
    }

    public static KeyBinding[] all() {
        return new KeyBinding[]{PANEL, STOP, PAUSE};
    }
}
