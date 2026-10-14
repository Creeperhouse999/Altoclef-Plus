package altoclefplus.ui.logic;

/** The @gamer roadmap, in order. Done-predicates live in {@link PlanTracker}. */
public enum Milestone {
    WOOD("Wood"),
    CRAFTING_TABLE("Crafting table"),
    WOODEN_PICKAXE("Wooden pickaxe"),
    STONE_TOOLS("Stone pickaxe, axe, shovel"),
    FOOD("Food stocked"),
    STONE_SWORD("Stone sword"),
    IRON_INGOTS("Iron ingots"),
    IRON_KIT("Iron pickaxe, sword, shield, water bucket"),
    IRON_ARMOR("Full iron armor"),
    DIAMONDS("Diamonds"),
    DIAMOND_GEAR("Diamond pickaxe, sword, armor"),
    BEDS("Beds"),
    NETHER("Entered the Nether"),
    BLAZE_RODS("Blaze rods"),
    ENDER_PEARLS("Ender pearls"),
    EYES_OF_ENDER("Eyes of ender"),
    STRONGHOLD("Stronghold / end portal"),
    END("Entered the End"),
    DRAGON("Dragon killed");

    public final String label;

    Milestone(String label) {
        this.label = label;
    }
}
