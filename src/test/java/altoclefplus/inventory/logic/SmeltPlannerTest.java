package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmeltPlannerTest {
    @Test
    void sideJobOnlyWhenTheWaitIsLongEnough() {
        assertEquals(0, SmeltPlanner.sideJobSeconds(1, 0.2, 5), "about 8 s left: just wait");
        assertEquals(75, SmeltPlanner.sideJobSeconds(8, 0.5, 5), "7.5 items x 10 s");
        assertEquals(0, SmeltPlanner.sideJobSeconds(8, 0.5, 40), "plenty of coal already");
    }
}
