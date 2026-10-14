package altoclefplus.core.logic;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Minimal type-keyed publish/subscribe. A failing subscriber never affects the others. */
public final class PlusEventBus {
    private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> type, Consumer<T> handler) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add((Consumer<Object>) handler);
    }

    public void publish(Object event) {
        List<Consumer<Object>> list = subscribers.get(event.getClass());
        if (list == null) return;
        for (Consumer<Object> handler : list) {
            try {
                handler.accept(event);
            } catch (RuntimeException e) {
                System.err.println("[AltoClef Plus] event handler failed for " + event + ": " + e);
            }
        }
    }
}
