package altoclefplus.core;

import adris.altoclef.AltoClef;
import adris.altoclef.Settings;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import adris.altoclef.tasks.speedrun.BeatMinecraftConfig;
import altoclefplus.Plus;
import altoclefplus.core.logic.PlusConfig;

import java.lang.reflect.Field;

/**
 * Pushes the panel's basic options onto AltoClef's own settings (in memory only — AltoClef's files are never
 * written). Re-applied regularly, so a change in the panel takes effect within a couple of seconds.
 */
public final class BasicsApplier {
    private static Field avoidDungeonChests;

    private BasicsApplier() {}

    public static void apply(AltoClef mod) {
        PlusConfig.Basics b = Plus.config().basics;
        BeatMinecraftConfig gamer = BeatMinecraft2Task.getConfig();
        if (gamer != null) {
            gamer.sleepThroughNight = b.sleepAtNight;
            gamer.searchRuinedPortals = b.lootChests && b.lootRuinedPortals;
            gamer.searchDesertTemples = b.lootChests && b.lootDesertTemples;
            gamer.barterPearlsInsteadOfEndermanHunt = b.barterForPearls;
            gamer.targetEyes = b.eyesToCollect;
            gamer.minimumEyes = Math.min(gamer.minimumEyes, b.eyesToCollect);
        }
        Settings s = mod.getModSettings();
        if (s == null) return;
        try {
            if (avoidDungeonChests == null) {
                avoidDungeonChests = Settings.class.getDeclaredField("avoidSearchingDungeonChests");
                avoidDungeonChests.setAccessible(true);
            }
            avoidDungeonChests.setBoolean(s, !b.lootChests);
        } catch (ReflectiveOperationException e) {
            // Older/newer AltoClef without this field: looting toggle then only affects @gamer.
        }
    }
}
