package altoclefplus.core.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogBufferTest {
    @Test
    void evictsOldestWhenFull() {
        LogBuffer log = new LogBuffer(3);
        log.add("a", "1");
        log.add("a", "2");
        log.add("a", "3");
        log.add("b", "4");

        List<LogBuffer.LogLine> lines = log.snapshot();

        assertEquals(3, lines.size());
        assertEquals("2", lines.get(0).message());
        assertEquals("4", lines.get(2).message());
        assertEquals("b", lines.get(2).source());
    }

    @Test
    void stripsMinecraftFormattingCodes() {
        LogBuffer log = new LogBuffer(5);
        log.add("altoclef", "§2§l[Alto Clef] §rhello");

        assertEquals("[Alto Clef] hello", log.snapshot().get(0).message());
    }
}
