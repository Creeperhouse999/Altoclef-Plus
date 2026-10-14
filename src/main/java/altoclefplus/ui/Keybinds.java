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
    public static final KeyBinding PAUSE = new KeyBinding("key.altoclefplus.pause", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, CATEGORY);

    static {
        Map<String, Integer> order = KeyBindingAccessor.altoclefplus$getCategoryOrderMap();
        if (!order.containsKey(CATEGORY)) {
            int max = order.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            order.put(CATEGORY, max + 1);
        }
    }

    private Keybinds() {}

    public static KeyBinding[] all() {
        return new KeyBinding[]{PANEL, STOP, PAUSE};
    }
}
