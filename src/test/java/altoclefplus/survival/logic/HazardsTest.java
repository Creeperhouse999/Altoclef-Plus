package altoclefplus.survival.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HazardsTest {
    @Test
    void neverStepsIntoOrOntoPowderSnow() {
        assertTrue(Hazards.avoidCell("minecraft:powder_snow", "minecraft:stone"), "sinking in freezes you");
        assertTrue(Hazards.avoidCell("minecraft:air", "minecraft:powder_snow"), "standing on it sinks you in");
        assertTrue(Hazards.avoidCell("minecraft:wither_rose", "minecraft:grass_block"), "wither effect");
        assertFalse(Hazards.avoidCell("minecraft:snow", "minecraft:snow_block"), "normal snow is fine");
        assertFalse(Hazards.avoidCell("minecraft:air", "minecraft:grass_block"));
    }

    @Test
    void goesForFoodWhenStarvingWithNothingToEat() {
        assertTrue(Hazards.needFoodTrip(6, 0, false));
        assertFalse(Hazards.needFoodTrip(6, 3, false), "has food: AltoClef eats it");
        assertFalse(Hazards.needFoodTrip(12, 0, false), "not hungry yet");
        assertFalse(Hazards.needFoodTrip(2, 0, true), "@gamer collects food itself");
    }
}
