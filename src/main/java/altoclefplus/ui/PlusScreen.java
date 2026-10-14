package altoclefplus.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Base for our screens: never pauses the game, and AltoClef must not auto-close it. */
public abstract class PlusScreen extends Screen {
    protected PlusScreen(Text title) {
        super(title);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
