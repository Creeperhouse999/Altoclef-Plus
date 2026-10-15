package altoclefplus.build.logic;

/**
 * One build: which schematic, where, and how far along. Plain fields so it is saved as-is in the config
 * (per world), which is what lets a paused build survive a game restart.
 */
public final class BuildJob {
    public enum State { QUEUED, RUNNING, PAUSED, WAITING_APPROVAL, DONE }

    public long id;
    public String world = "";
    /** "builtin:house.litematic" (inside the mod) or "folder:house.litematic" (.minecraft/schematics). */
    public String file = "";
    public String name = "";
    public int originX, originY, originZ, rotation;
    /** Fill chests, signs and item frames with the schematic's contents. */
    public boolean fillContents = true;
    public State state = State.QUEUED;
    public int placed;
    public int total;
    /** Why it waits (missing or impossible blocks), shown in the Build tab. */
    public String note = "";
    /** You approved building without the blocks survival can't place (command blocks, spawners...). */
    public boolean approvedSkipImpossible;
    /** Block ids you chose to leave out (e.g. an item the bot can't get). */
    public java.util.List<String> skipBlocks = new java.util.ArrayList<>();
    /** Blocks that need the item it can't get; "Approve" adds them to {@link #skipBlocks}. */
    public java.util.List<String> pendingSkip = new java.util.ArrayList<>();

    public BuildJob() {}

    public static BuildJob create(String world, String file, String name, Placement p, boolean fillContents) {
        BuildJob j = new BuildJob();
        j.id = System.nanoTime();
        j.world = world;
        j.file = file;
        j.name = name;
        j.setPlacement(p);
        j.fillContents = fillContents;
        return j;
    }

    public Placement placement() {
        return new Placement(originX, originY, originZ, rotation);
    }

    public void setPlacement(Placement p) {
        originX = p.originX();
        originY = p.originY();
        originZ = p.originZ();
        rotation = p.rotation();
    }
}
