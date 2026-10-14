package altoclefplus.compat;

import altoclefplus.ui.PanelScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Mod Menu's config button opens the panel on the Settings tab. Only loaded when Mod Menu is installed. */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new PanelScreen(parent, PanelScreen.Tab.SETTINGS);
    }
}
