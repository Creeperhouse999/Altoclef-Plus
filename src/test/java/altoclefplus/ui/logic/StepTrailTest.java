package altoclefplus.ui.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StepTrailTest {
    @Test
    void appendsOnChangeAndRecordsDurationOfPrevious() {
        StepTrail trail = new StepTrail(50);
        trail.update("<Collect wood>", 1000);
        trail.update("<Collect wood>", 2000);
        trail.update("<Craft pickaxe>", 5000);

        List<StepTrail.Step> steps = trail.entries();
        assertEquals(2, steps.size());
        assertEquals("<Collect wood>", steps.get(0).label());
        assertEquals(4000, steps.get(0).durationMs());
        assertTrue(steps.get(0).done());
        assertFalse(steps.get(1).done());
    }

    @Test
    void ignoresEmptyLabels() {
        StepTrail trail = new StepTrail(50);
        trail.update("", 1000);
        trail.update(null, 1000);

        assertTrue(trail.entries().isEmpty());
    }

    @Test
    void capsAtLimit() {
        StepTrail trail = new StepTrail(3);
        for (int i = 0; i < 5; i++) trail.update("step " + i, i * 1000L);

        List<StepTrail.Step> steps = trail.entries();
        assertEquals(3, steps.size());
        assertEquals("step 2", steps.get(0).label());
    }

    @Test
    void clearEmptiesTrail() {
        StepTrail trail = new StepTrail(5);
        trail.update("a", 0);
        trail.clear();

        assertTrue(trail.entries().isEmpty());
    }
}
