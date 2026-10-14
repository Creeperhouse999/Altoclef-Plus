package altoclefplus;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientTickEvent;
import altoclefplus.core.StateSampler;
import altoclefplus.core.logic.ConfigStore;
import altoclefplus.core.logic.LogBuffer;
import altoclefplus.core.logic.ModuleRegistry;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.PlusEventBus;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.core.logic.events.ModuleError;
import net.fabricmc.loader.api.FabricLoader;

/** Static service holder for the mod. Everything is created once AltoClef has initialized. */
public final class Plus {
    private static AltoClef mod;
    private static ConfigStore configStore;
    private static PlusConfig config = new PlusConfig();
    private static final PlusEventBus BUS = new PlusEventBus();
    private static final LogBuffer LOG = new LogBuffer(500);
    private static ModuleRegistry modules;
    private static StateSampler sampler;
    private static Snapshot last = Snapshot.builder().build();

    private Plus() {}

    public static void init(AltoClef altoClef) {
        mod = altoClef;
        configStore = new ConfigStore(FabricLoader.getInstance().getGameDir().resolve("altoclef").resolve("altoclefplus.json"));
        config = configStore.load();
        if (configStore.lastError() != null) LOG.add("plus", configStore.lastError());

        modules = new ModuleRegistry(BUS);
        BUS.subscribe(ModuleError.class, e -> LOG.add("plus", "Module '" + e.moduleId() + "' disabled after error: " + e.message()));

        sampler = new StateSampler(() -> "");
        EventBus.subscribe(ClientTickEvent.class, evt -> onTick());
        LOG.add("plus", "AltoClef Plus ready");
    }

    private static void onTick() {
        if (!AltoClef.inGame()) return;
        last = sampler.sample(mod);
        modules.tickAll(last);
    }

    public static AltoClef mod() {
        return mod;
    }

    public static PlusConfig config() {
        return config;
    }

    public static void saveConfig() {
        if (configStore != null) configStore.save(config);
    }

    public static PlusEventBus bus() {
        return BUS;
    }

    public static LogBuffer log() {
        return LOG;
    }

    public static ModuleRegistry modules() {
        return modules;
    }

    /** Most recent snapshot (never null). */
    public static Snapshot snapshot() {
        return last;
    }
}
