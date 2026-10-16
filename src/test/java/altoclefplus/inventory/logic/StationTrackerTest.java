package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StationTrackerTest {
    @Test
    void onlyTablesTheBotPlacedAreItsOwn() {
        StationTracker t = new StationTracker();
        t.observe(Set.of(1L), false, 0);          // a village table seen before
        t.observe(Set.of(1L, 2L), true, 10);      // ours appears while placing
        t.observe(Set.of(1L, 2L, 3L), false, 20); // someone else's appears, not while placing
        assertEquals(Set.of(2L), t.ours());
    }

    @Test
    void picksItsOwnUpAfterAShortPauseWhileStillClose() {
        StationTracker t = new StationTracker();
        t.observe(Set.of(), false, 0);
        t.observe(Set.of(7L), true, 10);
        assertEquals(Optional.empty(), t.toCollect(Map.of(7L, 2.0), true, 40), "still using it");
        assertEquals(Optional.empty(), t.toCollect(Map.of(7L, 2.0), false, 50), "just stopped using it");
        assertEquals(Optional.empty(), t.toCollect(Map.of(7L, 2.0), false, 100), "3 s not reached");
        assertEquals(Optional.of(7L), t.toCollect(Map.of(7L, 2.0), false, 120));
    }

    @Test
    void forgetsStationsLeftBehindOrGone() {
        StationTracker t = new StationTracker();
        t.observe(Set.of(), false, 0);
        t.observe(Set.of(7L), true, 10);
        assertEquals(Optional.empty(), t.toCollect(Map.of(7L, 12.0), false, 200), "too far: not worth going back");
        assertFalse(t.ours().contains(7L));
        t.observe(Set.of(8L), true, 210);
        t.collected(8L);
        assertFalse(t.ours().contains(8L));
    }
}
