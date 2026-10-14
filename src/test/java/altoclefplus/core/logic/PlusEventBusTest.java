package altoclefplus.core.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlusEventBusTest {
    record Ping(int n) {}
    record Pong(String s) {}

    @Test
    void deliversOnlyToSubscribersOfThatType() {
        PlusEventBus bus = new PlusEventBus();
        List<Object> got = new ArrayList<>();
        bus.subscribe(Ping.class, got::add);

        bus.publish(new Ping(1));
        bus.publish(new Pong("x"));

        assertEquals(List.of(new Ping(1)), got);
    }

    @Test
    void throwingSubscriberDoesNotBlockOthers() {
        PlusEventBus bus = new PlusEventBus();
        List<Ping> got = new ArrayList<>();
        bus.subscribe(Ping.class, p -> { throw new RuntimeException("boom"); });
        bus.subscribe(Ping.class, got::add);

        bus.publish(new Ping(2));

        assertEquals(List.of(new Ping(2)), got);
    }
}
