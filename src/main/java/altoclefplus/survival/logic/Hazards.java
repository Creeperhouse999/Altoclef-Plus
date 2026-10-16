package altoclefplus.survival.logic;

import java.util.Set;

/**
 * Hazards Baritone doesn't know about. It walks onto powder snow (snowy slopes, tundra), sinks in and freezes
 * to death, and walks through wither roses. Also: on a normal task AltoClef only eats food it already carries,
 * so a long "get" in a barren biome starved the bot.
 */
public final class Hazards {
    /** Blocks the bot must never be inside. */
    private static final Set<String> NO_ENTER = Set.of("minecraft:powder_snow", "minecraft:wither_rose");
    /** Blocks the bot must never stand on. */
    private static final Set<String> NO_STAND = Set.of("minecraft:powder_snow");

    private Hazards() {}

    /** @param block the cell the bot would occupy; @param below the block under it */
    public static boolean avoidCell(String block, String below) {
        return NO_ENTER.contains(block) || NO_STAND.contains(below);
    }

    /** Hunger at or below 6 (no sprinting, close to starving) with nothing to eat, outside @gamer. */
    public static boolean needFoodTrip(int hunger, int foodUnits, boolean gamer) {
        return !gamer && hunger <= 6 && foodUnits == 0;
    }
}
