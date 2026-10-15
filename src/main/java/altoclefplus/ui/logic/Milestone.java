package altoclefplus.ui.logic;

/** The @gamer roadmap, in order. Done-predicates live in {@link PlanTracker}. */
public enum Milestone {
    WOOD("Get wood"),
    CRAFTING_TABLE("Make a crafting table"),
    WOODEN_PICKAXE("Make a wooden pickaxe"),
    STONE_TOOLS("Make stone pickaxe, axe, shovel"),
    FOOD("Stock up food"),
    STONE_SWORD("Make a stone sword"),
    IRON_INGOTS("Get iron ingots"),
    IRON_KIT("Make iron pickaxe, sword, shield, bucket"),
    IRON_ARMOR("Make full iron armor"),
    DIAMONDS("Find diamonds"),
    DIAMOND_GEAR("Make diamond pickaxe, sword, armor"),
    BEDS("Get beds"),
    NETHER("Enter the Nether"),
    BLAZE_RODS("Get blaze rods"),
    ENDER_PEARLS("Get ender pearls"),
    EYES_OF_ENDER("Craft eyes of ender"),
    STRONGHOLD("Find the stronghold / end portal"),
    END("Enter the End"),
    DRAGON("Kill the dragon");

    public final String label;

    Milestone(String label) {
        this.label = label;
    }
}
