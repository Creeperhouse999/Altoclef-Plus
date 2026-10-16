package altoclefplus.survival;

import altoclefplus.Plus;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * AltoClef's @gamer collects and wears diamond armor with golden boots for the whole overworld part, and only
 * switches to diamond boots for the End. Swap the boots in that set for diamond ones: the bot wears full
 * diamond outside the Nether, and Plus puts golden boots on at the portal (gear gate + armor planner).
 */
public final class GamerArmor {
    private GamerArmor() {}

    public static void diamondBootsOutsideNether() {
        try {
            var f = adris.altoclef.tasks.speedrun.BeatMinecraft2Task.class.getDeclaredField("COLLECT_EYE_ARMOR");
            f.setAccessible(true);
            Item[] set = (Item[]) f.get(null);
            for (int i = 0; i < set.length; i++) if (set[i] == Items.GOLDEN_BOOTS) set[i] = Items.DIAMOND_BOOTS;
        } catch (ReflectiveOperationException | RuntimeException e) {
            Plus.log().add("plus", "Could not change @gamer boots: " + e);
        }
    }
}
