package altoclefplus.survival.logic;

import altoclefplus.core.logic.ItemInfo;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Progression gates: gear up before taking the next risk. Driven only by inventory/equipment, so it works
 * for @gamer and every other task. A gate that keeps failing is skipped for a while so the bot never
 * deadlocks on an item it can't get.
 */
public final class GatePlanner {
    public enum Gate { STONE_KIT, IRON_KIT, IRON_ARMOR, NETHER_ENTRY }

    /** An item to obtain before continuing. item "food" means food units (count) rather than an item id. */
    public record Need(Gate gate, String item, int count) {}

    private static final String[] ARMOR_ORDER = {"chest", "legs", "head", "feet"};

    private final Map<Gate, Integer> failures = new EnumMap<>(Gate.class);
    private final Map<Gate, Long> skipUntil = new EnumMap<>(Gate.class);
    private int failuresBeforeSkip = 3;
    private long skipMs = 10 * 60_000L;

    public Optional<Need> next(Snapshot s, PlusConfig.Survival cfg, long nowMs) {
        failuresBeforeSkip = cfg.gateFailuresBeforeSkip;
        skipMs = cfg.gateSkipMinutes * 60_000L;
        int pick = ItemInfo.bestTier(s, "pickaxe");

        if (active(Gate.STONE_KIT, cfg.gateStoneKit, nowMs) && pick >= ItemInfo.STONE) {
            for (String type : new String[]{"sword", "axe", "shovel"}) {
                if (ItemInfo.bestTier(s, type) < ItemInfo.STONE) return need(Gate.STONE_KIT, "stone_" + type);
            }
        }
        if (active(Gate.IRON_KIT, cfg.gateIronKit, nowMs) && pick >= ItemInfo.IRON) {
            if (ItemInfo.bestTier(s, "sword") < ItemInfo.IRON) return need(Gate.IRON_KIT, "iron_sword");
            if (!hasShield(s)) return need(Gate.IRON_KIT, "shield");
            if (!s.has("water_bucket")) return need(Gate.IRON_KIT, "water_bucket");
        }
        if (active(Gate.IRON_ARMOR, cfg.gateIronArmor, nowMs) && pick >= ItemInfo.IRON) {
            Optional<Need> armor = missingIronArmor(s, Gate.IRON_ARMOR);
            if (armor.isPresent()) return armor;
        }
        if (active(Gate.NETHER_ENTRY, cfg.gateNetherEntry, nowMs) && headingToNether(s)) {
            Optional<Need> armor = missingIronArmor(s, Gate.NETHER_ENTRY);
            if (armor.isPresent()) return armor;
            if (!hasShield(s)) return need(Gate.NETHER_ENTRY, "shield");
            if (ItemInfo.totalFoodUnits(s) < cfg.netherMinFoodUnits) {
                return Optional.of(new Need(Gate.NETHER_ENTRY, "food", cfg.netherMinFoodUnits));
            }
        }
        return Optional.empty();
    }

    public void markFailed(Gate gate, long nowMs) {
        int n = failures.merge(gate, 1, Integer::sum);
        if (n >= failuresBeforeSkip) {
            skipUntil.put(gate, nowMs + skipMs);
            failures.put(gate, 0);
        }
    }

    public boolean isSkipped(Gate gate, long nowMs) {
        return skipUntil.getOrDefault(gate, Long.MIN_VALUE) > nowMs;
    }

    private boolean active(Gate gate, boolean enabled, long nowMs) {
        return enabled && !isSkipped(gate, nowMs);
    }

    private static Optional<Need> need(Gate gate, String item) {
        return Optional.of(new Need(gate, item, 1));
    }

    private static Optional<Need> missingIronArmor(Snapshot s, Gate gate) {
        for (String slot : ARMOR_ORDER) {
            int t = ItemInfo.bestArmorTier(s, slot);
            if (t < ItemInfo.IRON) return need(gate, "iron_" + piece(slot));
        }
        return Optional.empty();
    }

    private static String piece(String slot) {
        return switch (slot) {
            case "chest" -> "chestplate";
            case "legs" -> "leggings";
            case "head" -> "helmet";
            default -> "boots";
        };
    }

    private static boolean hasShield(Snapshot s) {
        return s.has("shield") || "shield".equals(s.offhand);
    }

    /** @gamer says "Going to Nether", or a portal task is running, while still in the Overworld. */
    static boolean headingToNether(Snapshot s) {
        if (!"overworld".equals(s.dimension)) return false;
        for (String t : s.taskChain) {
            String l = t.toLowerCase(Locale.ROOT);
            if (l.contains("going to nether") || l.contains("nether portal")) return true;
        }
        return false;
    }
}
