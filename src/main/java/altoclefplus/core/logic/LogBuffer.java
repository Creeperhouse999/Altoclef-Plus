package altoclefplus.core.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Fixed-size ring of log lines shown in the UI instead of chat. */
public final class LogBuffer {
    public record LogLine(long timeMs, String source, String message) {}

    private final int capacity;
    private final ArrayDeque<LogLine> lines = new ArrayDeque<>();

    public LogBuffer(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void add(String source, String message) {
        // Strip Minecraft formatting codes (section sign + one char).
        String clean = message == null ? "" : message.replaceAll("§.", "");
        lines.addLast(new LogLine(System.currentTimeMillis(), source, clean));
        while (lines.size() > capacity) lines.removeFirst();
    }

    public synchronized List<LogLine> snapshot() {
        return new ArrayList<>(lines);
    }
}
