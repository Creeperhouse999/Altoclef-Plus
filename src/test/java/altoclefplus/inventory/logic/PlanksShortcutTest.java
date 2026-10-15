package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanksShortcutTest {
    @Test
    void wantsPlanksWhenAltoClefGoesForLogsToMakeThem() {
        List<String> chain = List.of("<Get stick>", "<Crafting 2 planks [acacia_planks, oak_planks]: [[acacia_planks,oak_planks] x 2]>",
                "<Mine And Collect: [[acacia_log,oak_log] x 1]>", "<Destroy block at 1, 2, 3>");
        assertEquals(2, PlanksShortcut.planksWanted(chain));
    }

    @Test
    void notWhenItAlreadyCraftsOrDoesSomethingElse() {
        assertEquals(0, PlanksShortcut.planksWanted(List.of("<Crafting 4 planks [oak_planks]: [[oak_planks] x 4]>",
                "<Doing stuff in crafting_table x 1 container>")), "has logs already: no trip");
        assertEquals(0, PlanksShortcut.planksWanted(List.of("<Mine And Collect: [[oak_log] x 10]>")), "you asked for logs");
    }
}
