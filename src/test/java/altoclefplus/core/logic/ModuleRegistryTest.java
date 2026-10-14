package altoclefplus.core.logic;

import altoclefplus.core.logic.events.ModuleError;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModuleRegistryTest {
    static class CountingModule implements Module {
        final String id;
        int ticks;
        boolean explode;

        CountingModule(String id) { this.id = id; }

        @Override public String id() { return id; }
        @Override public void tick(Snapshot s) {
            ticks++;
            if (explode) throw new IllegalStateException("kaboom");
        }
        @Override public String status() { return "ok"; }
    }

    @Test
    void throwingModuleIsDisabledAndOthersKeepTicking() {
        PlusEventBus bus = new PlusEventBus();
        List<ModuleError> errors = new ArrayList<>();
        bus.subscribe(ModuleError.class, errors::add);
        ModuleRegistry reg = new ModuleRegistry(bus);
        CountingModule bad = new CountingModule("bad");
        bad.explode = true;
        CountingModule good = new CountingModule("good");
        reg.register(bad, true);
        reg.register(good, true);

        reg.tickAll(Snapshot.builder().build());
        reg.tickAll(Snapshot.builder().build());

        assertEquals(1, bad.ticks);
        assertEquals(2, good.ticks);
        assertFalse(reg.isEnabled("bad"));
        assertNotNull(reg.failure("bad"));
        assertEquals(1, errors.size());
        assertEquals("bad", errors.get(0).moduleId());
    }

    @Test
    void disabledModuleIsNotTicked() {
        ModuleRegistry reg = new ModuleRegistry(new PlusEventBus());
        CountingModule m = new CountingModule("m");
        reg.register(m, false);

        reg.tickAll(Snapshot.builder().build());
        reg.setEnabled("m", true);
        reg.tickAll(Snapshot.builder().build());

        assertEquals(1, m.ticks);
    }

    @Test
    void reEnablingFailedModuleClearsFailure() {
        ModuleRegistry reg = new ModuleRegistry(new PlusEventBus());
        CountingModule m = new CountingModule("m");
        m.explode = true;
        reg.register(m, true);
        reg.tickAll(Snapshot.builder().build());

        m.explode = false;
        reg.setEnabled("m", true);

        assertNull(reg.failure("m"));
        assertTrue(reg.isEnabled("m"));
    }
}
