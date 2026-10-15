package altoclefplus.build.logic;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Bill of materials: which items a schematic needs (not which blocks — a door is one item for two blocks, a
 * wall torch is placed from a torch, a double slab takes two slabs), plus the blocks that can't be placed in
 * survival at all (command blocks, spawners, barriers...), which make the build stop and ask.
 */
public record Materials(Map<String, Integer> items, Map<String, Integer> unplaceable) {

    /** Blocks no survival player can place. */
    public static final Set<String> SURVIVAL_IMPOSSIBLE = Set.of(
            "minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block",
            "minecraft:spawner", "minecraft:barrier", "minecraft:structure_block", "minecraft:jigsaw",
            "minecraft:bedrock", "minecraft:end_portal", "minecraft:end_portal_frame", "minecraft:end_gateway",
            "minecraft:nether_portal", "minecraft:budding_amethyst", "minecraft:reinforced_deepslate",
            "minecraft:petrified_oak_slab", "minecraft:light", "minecraft:frosted_ice",
            "minecraft:infested_stone", "minecraft:infested_cobblestone", "minecraft:infested_stone_bricks",
            "minecraft:infested_mossy_stone_bricks", "minecraft:infested_cracked_stone_bricks",
            "minecraft:infested_chiseled_stone_bricks", "minecraft:infested_deepslate");

    /** Not placed at all (they appear on their own or are part of another block). */
    private static final Set<String> IGNORED = Set.of("minecraft:air", "minecraft:cave_air", "minecraft:void_air",
            "minecraft:structure_void", "minecraft:piston_head", "minecraft:moving_piston", "minecraft:fire",
            "minecraft:soul_fire", "minecraft:bubble_column", "minecraft:flowing_water", "minecraft:flowing_lava");

    /** Block id -> item id where they differ. */
    private static final Map<String, String> ITEM_FOR = Map.ofEntries(
            Map.entry("minecraft:wall_torch", "minecraft:torch"),
            Map.entry("minecraft:soul_wall_torch", "minecraft:soul_torch"),
            Map.entry("minecraft:redstone_wall_torch", "minecraft:redstone_torch"),
            Map.entry("minecraft:redstone_wire", "minecraft:redstone"),
            Map.entry("minecraft:tripwire", "minecraft:string"),
            Map.entry("minecraft:water", "minecraft:water_bucket"),
            Map.entry("minecraft:lava", "minecraft:lava_bucket"),
            Map.entry("minecraft:powder_snow", "minecraft:powder_snow_bucket"),
            Map.entry("minecraft:farmland", "minecraft:dirt"),
            Map.entry("minecraft:dirt_path", "minecraft:dirt"),
            Map.entry("minecraft:carrots", "minecraft:carrot"),
            Map.entry("minecraft:potatoes", "minecraft:potato"),
            Map.entry("minecraft:beetroots", "minecraft:beetroot_seeds"),
            Map.entry("minecraft:wheat", "minecraft:wheat_seeds"),
            Map.entry("minecraft:melon_stem", "minecraft:melon_seeds"),
            Map.entry("minecraft:attached_melon_stem", "minecraft:melon_seeds"),
            Map.entry("minecraft:pumpkin_stem", "minecraft:pumpkin_seeds"),
            Map.entry("minecraft:attached_pumpkin_stem", "minecraft:pumpkin_seeds"),
            Map.entry("minecraft:cocoa", "minecraft:cocoa_beans"),
            Map.entry("minecraft:sweet_berry_bush", "minecraft:sweet_berries"),
            Map.entry("minecraft:cave_vines", "minecraft:glow_berries"),
            Map.entry("minecraft:cave_vines_plant", "minecraft:glow_berries"),
            Map.entry("minecraft:kelp_plant", "minecraft:kelp"),
            Map.entry("minecraft:twisting_vines_plant", "minecraft:twisting_vines"),
            Map.entry("minecraft:weeping_vines_plant", "minecraft:weeping_vines"),
            Map.entry("minecraft:bamboo_sapling", "minecraft:bamboo"),
            Map.entry("minecraft:big_dripleaf_stem", "minecraft:big_dripleaf"),
            Map.entry("minecraft:tall_seagrass", "minecraft:seagrass"));

    public static Materials of(Schematic s) {
        Map<String, Integer> items = new TreeMap<>();
        Map<String, Integer> impossible = new TreeMap<>();
        for (int c : s.cells()) {
            if (c != Schematic.NONE) addState(s.palette()[c], items, impossible);
        }
        return new Materials(items, impossible);
    }

    /** Adds what one block state needs to {@code items}, or to {@code impossible} if survival can't place it. */
    public static void addState(String state, Map<String, Integer> items, Map<String, Integer> impossible) {
        String block = Schematic.blockId(state);
        if (IGNORED.contains(block)) return;
        if (SURVIVAL_IMPOSSIBLE.contains(block)) {
            impossible.merge(block, 1, Integer::sum);
            return;
        }
        // Second halves and fluid flow carry no item of their own.
        if (has(state, "half=upper") || has(state, "part=head")) return;
        if ((block.equals("minecraft:water") || block.equals("minecraft:lava")) && state.contains("level=") && !has(state, "level=0")) return;
        if (block.startsWith("minecraft:potted_")) {
            items.merge("minecraft:flower_pot", 1, Integer::sum);
            items.merge("minecraft:" + block.substring("minecraft:potted_".length()), 1, Integer::sum);
            return;
        }
        int count = 1;
        if (has(state, "type=double") && block.endsWith("_slab")) count = 2;
        count = Math.max(count, number(state, "candles"));
        count = Math.max(count, number(state, "pickles"));
        count = Math.max(count, number(state, "eggs"));
        count = Math.max(count, number(state, "layers"));
        items.merge(itemFor(block), count, Integer::sum);
    }

    /** What is still missing after counting what we already have (carried, in reach, already placed...). */
    public Map<String, Integer> stillNeeded(Map<String, Integer> have) {
        Map<String, Integer> out = new TreeMap<>();
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            int missing = e.getValue() - have.getOrDefault(e.getKey(), 0);
            if (missing > 0) out.put(e.getKey(), missing);
        }
        return out;
    }

    public static String itemFor(String block) {
        String mapped = ITEM_FOR.get(block);
        if (mapped != null) return mapped;
        // Wall-mounted variants are placed from the normal item: oak_wall_sign -> oak_sign, ..._wall_head -> ..._head
        if (block.contains("_wall_")) return block.replace("_wall_", "_");
        return block;
    }

    private static boolean has(String state, String prop) {
        return state.contains("[" + prop + "]") || state.contains("[" + prop + ",") || state.contains("," + prop + ",")
                || state.contains("," + prop + "]");
    }

    private static int number(String state, String key) {
        int i = state.indexOf(key + "=");
        if (i < 0) return 0;
        int j = i + key.length() + 1;
        int k = j;
        while (k < state.length() && Character.isDigit(state.charAt(k))) k++;
        return k > j ? Integer.parseInt(state.substring(j, k)) : 0;
    }
}
