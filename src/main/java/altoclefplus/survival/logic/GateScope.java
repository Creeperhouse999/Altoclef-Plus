package altoclefplus.survival.logic;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Which gear gates apply to the running task. By default (NEEDED) only @gamer and tasks whose items need gear
 * get gated: deep ores need the basic kit, Nether items need Nether gear, End items need everything. Plain
 * errands like "get dirt" start right away.
 */
public final class GateScope {
    public enum Scope { NEEDED, GAMER_ONLY, ALL }

    private static final Set<String> NETHER = Set.of(
            "quartz", "nether_quartz_ore", "netherrack", "nether_wart", "blaze_rod", "blaze_powder", "glowstone",
            "glowstone_dust", "soul_sand", "soul_soil", "magma_cream", "magma_block", "ghast_tear", "ancient_debris",
            "netherite_scrap", "netherite_ingot", "basalt", "blackstone", "nether_bricks", "nether_brick",
            "crimson_stem", "warped_stem", "crimson_planks", "warped_planks", "crimson_nylium", "warped_nylium",
            "shroomlight", "weeping_vines", "twisting_vines", "gilded_blackstone", "wither_skeleton_skull",
            "crying_obsidian", "warped_wart_block", "nether_wart_block", "quartz_block");
    private static final Set<String> END = Set.of(
            "end_stone", "end_stone_bricks", "chorus_fruit", "chorus_flower", "popped_chorus_fruit", "purpur_block",
            "purpur_pillar", "shulker_shell", "shulker_box", "elytra", "dragon_egg", "dragon_breath", "end_rod");
    private static final Set<String> DEEP = Set.of(
            "diamond", "diamond_ore", "deepslate_diamond_ore", "redstone", "redstone_ore", "lapis_lazuli",
            "lapis_ore", "gold_ingot", "raw_gold", "gold_ore", "emerald", "emerald_ore", "obsidian", "amethyst_shard",
            "deepslate", "cobbled_deepslate", "diamond_block", "gold_block", "redstone_block", "lapis_block");

    private GateScope() {}

    public static Set<GatePlanner.Gate> allowed(Scope scope, String command, Set<String> taskTargets) {
        String cmd = command == null ? "" : command.trim();
        boolean gamer = cmd.startsWith("gamer");
        if (gamer) return EnumSet.allOf(GatePlanner.Gate.class);
        if (scope == Scope.ALL) return EnumSet.complementOf(EnumSet.of(GatePlanner.Gate.EARLY_BED));
        if (scope == Scope.GAMER_ONLY) return EnumSet.noneOf(GatePlanner.Gate.class);

        Set<String> targets = new HashSet<>(taskTargets);
        targets.addAll(itemsInCommand(cmd));
        if (targets.stream().anyMatch(END::contains)) return EnumSet.complementOf(EnumSet.of(GatePlanner.Gate.EARLY_BED));
        if (targets.stream().anyMatch(NETHER::contains)) {
            return EnumSet.of(GatePlanner.Gate.STONE_KIT, GatePlanner.Gate.IRON_KIT,
                    GatePlanner.Gate.IRON_ARMOR, GatePlanner.Gate.NETHER_ENTRY);
        }
        if (targets.stream().anyMatch(DEEP::contains)) {
            return EnumSet.of(GatePlanner.Gate.STONE_KIT, GatePlanner.Gate.IRON_KIT);
        }
        return EnumSet.noneOf(GatePlanner.Gate.class);
    }

    /** "get quartz 4; get dirt 2" -> {quartz, dirt}. */
    static Set<String> itemsInCommand(String cmd) {
        Set<String> items = new HashSet<>();
        for (String part : cmd.split(";")) {
            String[] words = part.trim().split("\\s+");
            if (words.length >= 2 && words[0].equals("get")) {
                String id = words[1].toLowerCase();
                if (id.startsWith("minecraft:")) id = id.substring("minecraft:".length());
                items.add(id);
            }
        }
        return items;
    }
}
