package altoclefplus.ui.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskNarratorTest {
    @Test
    void goalFromCommand() {
        assertEquals("Get iron boots", TaskNarrator.goal("get iron_boots 1"));
        assertEquals("Get spruce log x40", TaskNarrator.goal("get spruce_log 40"));
        assertEquals("Beat the game", TaskNarrator.goal("gamer"));
        assertEquals("Food (20)", TaskNarrator.goal("food 20"));
        assertEquals("", TaskNarrator.goal(""));
    }

    @Test
    void deepestKnownStepWins() {
        List<String> chain = List.of(
                "<Doing stuff in crafting_table x 1 container: [[iron_pickaxe] x 1]>",
                "<Mine And Collect: [[iron_ore,deepslate_iron_ore] x 3]> Getting",
                "<Mining or Collecting> Trying out NEW pursuit",
                "<Destroy block at -116, 32, 41> Getting to block...");
        assertEquals("Mining iron ore", TaskNarrator.action(chain, "iron_ore"));
        assertEquals("Collecting iron ore", TaskNarrator.action(chain, ""), "block unknown: fall back to the collect step");
    }

    @Test
    void craftingSmeltingAndFighting() {
        assertEquals("Crafting iron boots", TaskNarrator.action(List.of(
                "<Doing stuff in crafting_table x 1 container>",
                "<Crafting (w/ RECIPE): RecipeTarget{_recipe=CraftingRecipe{craft iron_boots}, _item=iron_boots x 1}>"), ""));
        assertEquals("Smelting", TaskNarrator.action(List.of("<Doing stuff in [furnace] x 1 container> Waiting..."), ""));
        assertEquals("Fighting mobs", TaskNarrator.action(List.of("<Killing entities>"), ""));
        assertEquals("Running from a creeper", TaskNarrator.action(List.of("<Run away from creepers>"), ""));
        assertEquals("Picking up items", TaskNarrator.action(List.of("<Pickup Dropped Items: [[spruce_log] x 10]>"), ""));
        assertEquals("Exploring", TaskNarrator.action(List.of("<Wander for 5.0 blocks> Exploring"), ""));
    }

    @Test
    void unknownStepsShowCleanedName() {
        assertEquals("Some new thing", TaskNarrator.action(List.of("<Some new thing> debug text"), ""));
        assertEquals("", TaskNarrator.action(List.of(), ""));
    }

    @Test
    void wordsInsideOtherWordsDontMatch() {
        assertEquals("Beat Minecraft", TaskNarrator.action(List.of("<Beat Minecraft> Getting started"), ""), "not 'Eating'");
        assertEquals("Putting on armor", TaskNarrator.action(List.of("<Equipping armor [iron_chestplate]>"), ""));
        assertEquals("Eating", TaskNarrator.action(List.of("<Eat food>"), ""));
    }
}
