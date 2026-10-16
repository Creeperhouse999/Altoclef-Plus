package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CraftGridTest {
    @Test
    void theTwoByTwoGridOnlyExistsWithoutAnotherScreen() {
        assertTrue(CraftGrid.visible(true, true), "no screen or the player's inventory: grid is there");
        assertFalse(CraftGrid.visible(true, false), "furnace open: its slots 1-4 are not the crafting grid");
        assertTrue(CraftGrid.visible(false, false), "other player slots are mapped per screen by AltoClef");
    }
}
