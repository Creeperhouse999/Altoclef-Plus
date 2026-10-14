package altoclefplus.core.logic;

import altoclefplus.core.logic.events.ModuleError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Holds modules, ticks the enabled ones and isolates failures. */
public final class ModuleRegistry {
    private final PlusEventBus bus;
    private final Map<String, Module> modules = new LinkedHashMap<>();
    private final Map<String, Boolean> enabled = new HashMap<>();
    private final Map<String, String> failures = new HashMap<>();

    public ModuleRegistry(PlusEventBus bus) {
        this.bus = bus;
    }

    public void register(Module module, boolean on) {
        modules.put(module.id(), module);
        enabled.put(module.id(), on);
    }

    public void tickAll(Snapshot snapshot) {
        for (Module m : modules.values()) {
            if (!isEnabled(m.id())) continue;
            try {
                m.tick(snapshot);
            } catch (RuntimeException e) {
                String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
                failures.put(m.id(), msg);
                setEnabled(m.id(), false);
                bus.publish(new ModuleError(m.id(), msg));
            }
        }
    }

    public void setEnabled(String id, boolean on) {
        if (!modules.containsKey(id)) return;
        boolean was = isEnabled(id);
        enabled.put(id, on);
        if (on) failures.remove(id);
        if (was && !on) {
            try {
                modules.get(id).onDisable();
            } catch (RuntimeException ignored) {
                // Disabling must never fail.
            }
        }
    }

    public boolean isEnabled(String id) {
        return enabled.getOrDefault(id, false);
    }

    public String failure(String id) {
        return failures.get(id);
    }

    public Module get(String id) {
        return modules.get(id);
    }

    public List<String> ids() {
        return new ArrayList<>(modules.keySet());
    }
}
