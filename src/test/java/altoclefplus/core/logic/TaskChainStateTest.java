package altoclefplus.core.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskChainStateTest {
    @Test
    void busyWhilePlacingOrUsingAContainer() {
        // Seen in a real run: side jobs kept interrupting these, and the table got re-placed over and over.
        assertTrue(TaskChainState.inDelicateStep(List.of("<Doing stuff in crafting_table x 1 container: [[wooden_pickaxe] x 1]>",
                "<Doing stuff in crafting_table x 1 container> Placing container",
                "<Place structure{Block{minecraft:crafting_table}} at -80, 64, 25> Letting baritone place a block.")));
        assertTrue(TaskChainState.inDelicateStep(List.of("<Doing stuff in [furnace] x 1 container> Waiting...")));
        assertTrue(TaskChainState.inDelicateStep(List.of("<Get>", "<Crafting (w/ RECIPE): RecipeTarget{...}>", "<Clicking PlayerSlot{...}>")));
    }

    @Test
    void freeWhileMiningOrWalking() {
        assertFalse(TaskChainState.inDelicateStep(List.of("<Mine And Collect: [[iron_ore] x 3]>", "<Destroy block at 1, 2, 3>")));
        assertFalse(TaskChainState.inDelicateStep(List.of(
                "<Doing stuff in crafting_table x 1 container: [[iron_pickaxe] x 1]>", "<Mine And Collect: [[iron_ore] x 3]>",
                "<Destroy block at 1, 2, 3> Getting to block...")), "a crafting task that is really out mining: fine to interrupt");
        assertFalse(TaskChainState.inDelicateStep(List.of()));
    }
}
