package altoclefplus.ui.logic;

import java.util.List;

/** Ready-made presets shown in the Presets tab, grouped. Item names are AltoClef catalogue names. */
public final class PresetLibrary {
    public record Preset(String name, String command) {}

    public record Category(String name, List<Preset> presets) {}

    private PresetLibrary() {}

    private static Preset p(String name, String command) {
        return new Preset(name, command);
    }

    private static final List<Category> ALL = List.of(
            new Category("Tools", List.of(
                    p("Wood tools", "get [wooden_pickaxe 1, wooden_axe 1, wooden_shovel 1, wooden_sword 1]"),
                    p("Stone tools", "get [stone_pickaxe 1, stone_axe 1, stone_shovel 1, stone_sword 1]"),
                    p("Iron tools", "get [iron_pickaxe 1, iron_axe 1, iron_shovel 1, iron_sword 1]"),
                    p("Diamond tools", "get [diamond_pickaxe 1, diamond_axe 1, diamond_shovel 1, diamond_sword 1]"),
                    p("Shears", "get shears 1"),
                    p("Bow + 32 arrows", "get [bow 1, arrow 32]"),
                    p("Fishing rod", "get fishing_rod 1"),
                    p("Flint and steel", "get flint_and_steel 1"))),
            new Category("Armor", List.of(
                    p("Leather armor", "get [leather_helmet 1, leather_chestplate 1, leather_leggings 1, leather_boots 1]"),
                    p("Iron armor", "get [iron_helmet 1, iron_chestplate 1, iron_leggings 1, iron_boots 1]"),
                    p("Golden armor", "get [golden_helmet 1, golden_chestplate 1, golden_leggings 1, golden_boots 1]"),
                    p("Diamond armor", "get [diamond_helmet 1, diamond_chestplate 1, diamond_leggings 1, diamond_boots 1]"),
                    p("Shield", "get shield 1"))),
            new Category("Kits", List.of(
                    p("Wood tools + leather armor", "get [wooden_pickaxe 1, wooden_axe 1, wooden_sword 1, leather_helmet 1, "
                            + "leather_chestplate 1, leather_leggings 1, leather_boots 1]"),
                    p("Starter kit", "get [stone_pickaxe 1, stone_axe 1, stone_sword 1, crafting_table 1, torch 16, bread 8]"),
                    p("Mining kit", "get [iron_pickaxe 2, torch 64, bread 16, cobblestone 64, water_bucket 1]"),
                    p("Builder kit", "get [dirt 64, cobblestone 128, planks 64, glass 32, ladder 16]"),
                    p("Fighter kit", "get [iron_sword 1, shield 1, bow 1, arrow 64, iron_chestplate 1, cooked_beef 16]"),
                    p("Nether kit", "get [iron_pickaxe 1, iron_sword 1, golden_boots 1, flint_and_steel 1, obsidian 10, "
                            + "cooked_beef 16]"),
                    p("End kit", "get [diamond_sword 1, bow 1, arrow 64, water_bucket 1, ender_eye 12, cooked_beef 24]"),
                    p("Home kit", "get [bed 1, crafting_table 1, furnace 1, chest 2, torch 16]"))),
            new Category("Resources", List.of(
                    p("Logs x64", "get log 64"),
                    p("Planks x64", "get planks 64"),
                    p("Cobblestone x64", "get cobblestone 64"),
                    p("Dirt x64", "get dirt 64"),
                    p("Coal x32", "get coal 32"),
                    p("Iron x16", "get iron_ingot 16"),
                    p("Gold x16", "get gold_ingot 16"),
                    p("Diamonds x5", "get diamond 5"),
                    p("Obsidian x10", "get obsidian 10"),
                    p("Sand x64", "get sand 64"),
                    p("Glass x32", "get glass 32"),
                    p("Wool x16", "get wool 16"),
                    p("String x16", "get string 16"))),
            new Category("Food and base", List.of(
                    p("Food (20)", "food 20"),
                    p("Food (60)", "food 60"),
                    p("Bread x16", "get bread 16"),
                    p("Cooked beef x16", "get cooked_beef 16"),
                    p("Golden apple", "get golden_apple 1"),
                    p("Bed", "get bed 1"),
                    p("Torches x64", "get torch 64"),
                    p("Water bucket", "get water_bucket 1"),
                    p("Boat", "get boat 1"))),
            new Category("Speedrun", List.of(
                    p("Beat the game", "gamer"),
                    p("Blaze rods x7", "get blaze_rod 7"),
                    p("Ender pearls x12", "get ender_pearl 12"),
                    p("Eyes of ender x12", "get ender_eye 12"))));

    public static List<Category> all() {
        return ALL;
    }
}
