package altoclefplus;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import adris.altoclef.eventbus.events.ClientTickEvent;
import adris.altoclef.eventbus.events.TaskFinishedEvent;
import altoclefplus.core.StateSampler;
import altoclefplus.core.logic.ConfigStore;
import altoclefplus.core.logic.LogBuffer;
import altoclefplus.core.logic.ModuleRegistry;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.PlusEventBus;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.core.logic.events.ModuleError;
import altoclefplus.core.Hooks;
import altoclefplus.ui.AltoClefCommandRunner;
import altoclefplus.ui.Hud;
import altoclefplus.ui.UiModule;
import altoclefplus.ui.logic.BotControl;
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
    private static BotControl control;
    private static UiModule ui;

    private Plus() {}

    public static void init(AltoClef altoClef) {
        mod = altoClef;
        configStore = new ConfigStore(FabricLoader.getInstance().getGameDir().resolve("altoclef").resolve("altoclefplus.json"));
        config = configStore.load();
        if (configStore.lastError() != null) LOG.add("plus", configStore.lastError());

        modules = new ModuleRegistry(BUS);
        BUS.subscribe(ModuleError.class, e -> LOG.add("plus", "Module '" + e.moduleId() + "' disabled after error: " + e.message()));

        control = new BotControl(new AltoClefCommandRunner(mod));
        EventBus.subscribe(TaskFinishedEvent.class, evt -> control.onTaskFinished());
        configureBaritoneChat();

        try {
            AltoClef.getCommandExecutor().registerNewCommand(new altoclefplus.survival.HuntEndermenCommand());
        } catch (Exception e) {
            LOG.add("plus", "Could not register huntendermen command: " + e.getMessage());
        }
        altoclefplus.zones.Zones.registerWithBaritone(mod);
        altoclefplus.core.BasicsApplier.apply(mod);
        sampler = new StateSampler(control::lastCommand);
        registerModules();
        for (String hook : new String[]{"chat-block", "altoclef-log", "altoclef-overlay", "keep-panel-open", "camera", "fair-combat", "craft-grid-fix", "table-distance", "furnace-cost", "hotbar-fix", "tool-fix", "stronghold-estimate", "fast-travel", "obsidian-cast", "enderman-hunt", "door-path", "door-eat", "zone-guard", "steady-mining", "weapon-choice", "cave-water", "visible-inventory", "user-screen-pause", "keep-user-screen"}) {
            Hooks.expect(hook);
        }
        EventBus.subscribe(ClientTickEvent.class, evt -> {
            try {
                altoclefplus.core.DevBridge.tick();
            } catch (RuntimeException e) {
                reportOnce("dev-bridge", e);
            }
            try {
                onTick();
            } catch (RuntimeException e) {
                reportOnce("tick", e);
            }
        });
        EventBus.subscribe(ClientRenderEvent.class, evt -> {
            try {
                if (modules.isEnabled("ui") && AltoClef.inGame()) Hud.render(evt.stack, ui);
            } catch (RuntimeException e) {
                reportOnce("hud", e);
            }
        });
        LOG.add("plus", "AltoClef Plus ready");
    }

    private static void registerModules() {
        int targetEyes = 12;
        try {
            targetEyes = adris.altoclef.tasks.speedrun.BeatMinecraft2Task.getConfig().targetEyes;
        } catch (RuntimeException e) {
            LOG.add("plus", "Could not read @gamer config, assuming 12 eyes");
        }
        ui = new UiModule(targetEyes);
        modules.register(ui, config.modules.ui);
        modules.register(new altoclefplus.camera.CameraModule(), config.modules.camera);
        modules.register(new altoclefplus.inventory.InventoryModule(mod), config.modules.inventory);
        modules.register(new altoclefplus.survival.SurvivalModule(mod), config.modules.survival);
        modules.register(new altoclefplus.recovery.RecoveryModule(mod), config.modules.recovery);
    }

    /** Baritone: no chat commands, and its messages go to the Plus log. */
    private static void configureBaritoneChat() {
        baritone.api.Settings bs = mod.getClientBaritoneSettings();
        if (config.ui.blockChatCommands) {
            bs.prefixControl.value = false;
            bs.chatControl.value = false;
        }
        if (config.ui.redirectChat) {
            var original = bs.logger.value;
            bs.logger.value = text -> {
                if (config.modules.ui && config.ui.redirectChat) LOG.add("baritone", text.getString());
                else original.accept(text);
            };
        }
    }

    private static long basicsTick;
    private static final java.util.Set<String> REPORTED = new java.util.HashSet<>();

    /** AltoClef's event bus doesn't catch exceptions; never let ours crash the game. */
    private static void reportOnce(String where, RuntimeException e) {
        if (REPORTED.add(where + e.getClass().getName())) {
            LOG.add("plus", "Error in " + where + ": " + e);
            e.printStackTrace();
        }
    }

    private static void onTick() {
        if (!AltoClef.inGame()) return;
        altoclefplus.ui.Keybinds.poll();
        altoclefplus.zones.Zones.refresh();
        if (++basicsTick % 40 == 0) altoclefplus.core.BasicsApplier.apply(mod);
        altoclefplus.inventory.HotbarManager.onTick();
        altoclefplus.core.UserScreens.tick();
        altoclefplus.inventory.VisibleInventory.tick();
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

    public static UiModule ui() {
        return ui;
    }

    public static BotControl control() {
        return control;
    }

    /** Most recent snapshot (never null). */
    public static Snapshot snapshot() {
        return last;
    }
}
