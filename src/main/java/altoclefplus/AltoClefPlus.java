package altoclefplus;

import adris.altoclef.AltoClef;
import net.fabricmc.api.ModInitializer;

public class AltoClefPlus implements ModInitializer {
    @Override
    public void onInitialize() {
        AltoClef.subscribeToPostInit(Plus::init);
    }
}
