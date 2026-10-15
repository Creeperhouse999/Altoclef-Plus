package altoclefplus.ui.logic;

import altoclefplus.core.logic.ItemInfo;
import altoclefplus.core.logic.Snapshot;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Ticks @gamer milestones from inventory/world state. Ticks are monotonic until {@link #reset()}, so the
 * plan stays correct after pause/resume, recovery or losing items.
 */
public final class PlanTracker {
    public enum Status { DONE, CURRENT, TODO }

    public record MilestoneStatus(Milestone milestone, Status status) {}

    private static final int FOOD_UNITS = 20;
    private static final String[] STRONGHOLD_STATES = {
            "Opening End Portal", "Setting spawn near end portal", "Getting equipment for End", "Entering End"};

    private final int targetEyes;
    private final Set<Milestone> done = EnumSet.noneOf(Milestone.class);
    private Consumer<Milestone> listener = m -> {};

    public PlanTracker(int targetEyes) {
        this.targetEyes = targetEyes;
    }

    private boolean requireWaterBucket = true;

    /** Follows the "...and a water bucket" gear-gate setting. */
    public void setRequireWaterBucket(boolean require) {
        this.requireWaterBucket = require;
    }

    public void setListener(Consumer<Milestone> listener) {
        this.listener = listener;
    }

    public void reset() {
        done.clear();
    }

    public void update(Snapshot s) {
        for (Milestone m : Milestone.values()) {
            if (!done.contains(m) && reached(m, s)) {
                done.add(m);
                listener.accept(m);
            }
        }
    }

    public boolean isDone(Milestone m) {
        return done.contains(m);
    }

    /** First milestone not yet done, or null when all are done. */
    public Milestone current() {
        for (Milestone m : Milestone.values()) if (!done.contains(m)) return m;
        return null;
    }

    public List<MilestoneStatus> statuses() {
        List<MilestoneStatus> out = new ArrayList<>();
        Milestone cur = current();
        for (Milestone m : Milestone.values()) {
            Status st = done.contains(m) ? Status.DONE : (m == cur ? Status.CURRENT : Status.TODO);
            out.add(new MilestoneStatus(m, st));
        }
        return out;
    }

    private boolean reached(Milestone m, Snapshot s) {
        int pick = ItemInfo.bestTier(s, "pickaxe");
        return switch (m) {
            case WOOD -> ItemInfo.countMatching(s, id -> ItemInfo.isLog(id) || ItemInfo.isPlanks(id)) > 0
                    || s.has("crafting_table") || pick >= ItemInfo.WOOD;
            case CRAFTING_TABLE -> s.has("crafting_table") || pick >= ItemInfo.WOOD;
            case WOODEN_PICKAXE -> pick >= ItemInfo.WOOD;
            case STONE_TOOLS -> pick >= ItemInfo.STONE && ItemInfo.bestTier(s, "axe") >= ItemInfo.STONE
                    && ItemInfo.bestTier(s, "shovel") >= ItemInfo.STONE;
            case FOOD -> ItemInfo.totalFoodUnits(s) >= FOOD_UNITS;
            case STONE_SWORD -> ItemInfo.bestTier(s, "sword") >= ItemInfo.STONE;
            case IRON_INGOTS -> s.has("iron_ingot") || pick >= ItemInfo.IRON;
            case IRON_KIT -> pick >= ItemInfo.IRON && ItemInfo.bestTier(s, "sword") >= ItemInfo.IRON
                    && (s.has("shield") || "shield".equals(s.offhand))
                    && (!requireWaterBucket || s.has("water_bucket") || s.has("bucket"));
            case IRON_ARMOR -> allArmorAtLeast(s, ItemInfo.IRON);
            case DIAMONDS -> s.has("diamond") || pick >= ItemInfo.DIAMOND;
            case DIAMOND_GEAR -> pick >= ItemInfo.DIAMOND && ItemInfo.bestTier(s, "sword") >= ItemInfo.DIAMOND
                    && ItemInfo.bestArmorTier(s, "chest") >= ItemInfo.DIAMOND;
            case BEDS -> ItemInfo.countMatching(s, ItemInfo::isBed) > 0;
            case NETHER -> "the_nether".equals(s.dimension);
            case BLAZE_RODS -> s.count("blaze_rod") + (s.count("blaze_powder") + 1) / 2 + (s.count("ender_eye") + 1) / 2
                    >= (targetEyes + 1) / 2;
            case ENDER_PEARLS -> s.count("ender_pearl") + s.count("ender_eye") >= targetEyes;
            case EYES_OF_ENDER -> s.count("ender_eye") >= targetEyes;
            case STRONGHOLD -> "the_end".equals(s.dimension) || chainMentions(s, STRONGHOLD_STATES);
            case END -> "the_end".equals(s.dimension);
            case DRAGON -> chainMentions(s, new String[]{"WOOHOO"});
        };
    }

    private static boolean allArmorAtLeast(Snapshot s, int tier) {
        for (String slot : ItemInfo.ARMOR_SLOTS) {
            int t = ItemInfo.bestArmorTier(s, slot);
            if (t < tier || t == ItemInfo.GOLD) return false;
        }
        return true;
    }

    private static boolean chainMentions(Snapshot s, String[] needles) {
        for (String entry : s.taskChain) {
            for (String n : needles) if (entry.contains(n)) return true;
        }
        return false;
    }
}
