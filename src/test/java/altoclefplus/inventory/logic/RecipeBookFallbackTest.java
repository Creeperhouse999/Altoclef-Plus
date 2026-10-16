package altoclefplus.inventory.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeBookFallbackTest {
    @Test
    void craftsByHandWhenTheRecipeBookClickDoesNothingFor3Seconds() {
        RecipeBookFallback f = new RecipeBookFallback();
        assertFalse(f.waiting(1_000), "just clicked");
        assertFalse(f.waiting(3_500));
        assertTrue(f.waiting(4_000), "3 s without the grid filling (JEI/Create modpacks)");
        assertTrue(f.waiting(4_100), "stays manual");
    }

    @Test
    void progressResetsTheWait() {
        RecipeBookFallback f = new RecipeBookFallback();
        f.waiting(1_000);
        f.progressed();
        assertFalse(f.waiting(4_500), "the book worked last time: wait again from now");
    }
}
