package altoclefplus.build;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import altoclefplus.build.logic.BuildJob;
import altoclefplus.build.logic.Materials;
import altoclefplus.build.logic.Schematic;
import baritone.api.Settings;
import baritone.api.process.IBuilderProcess;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Builds one {@link BuildJob}: checks for blocks survival can't place (stops and asks), gathers what is
 * missing with AltoClef (chests first, with the chest rules), then lets Baritone's builder place it layer by
 * layer from the bottom, through our {@link PlacedSchematic} view that enforces safe zones and item frames.
 * Interruptions (eating, fighting) just restart the builder: it only places what is still missing.
 */
public final class BuildTask extends Task {
    public enum Outcome { NONE, DONE, WAITING_APPROVAL, FAILED }

    private final BuildJob job;
    private Schematic schematic;
    private PlacedSchematic placed;
    private BuildScanner scanner;
    private Outcome outcome = Outcome.NONE;
    private String why = "";
    private Boolean savedLayers, savedLayerOrder;
    private Integer savedLayerHeight;

    public BuildTask(BuildJob job) {
        this.job = job;
    }

    public BuildJob job() {
        return job;
    }

    public Outcome outcome() {
        return outcome;
    }

    public String why() {
        return why;
    }

    @Override
    protected void onStart(AltoClef mod) {
        try {
            schematic = SchematicLibrary.load(job.file);
        } catch (Exception e) {
            fail("Can't read " + job.file + ": " + e.getMessage());
            return;
        }
        Map<String, Integer> impossible = Materials.of(schematic).unplaceable();
        if (!impossible.isEmpty() && !job.approvedSkipImpossible) {
            waitFor("Can't place in survival: " + describe(impossible) + ". Approve to build the rest without them.");
            return;
        }
        placed = new PlacedSchematic(schematic, job.placement(), new HashSet<>(job.skipBlocks), Builds::mayTouch);
        scanner = new BuildScanner(placed);
        Settings bs = mod.getClientBaritoneSettings();
        savedLayers = bs.buildInLayers.value;
        savedLayerOrder = bs.layerOrder.value;
        savedLayerHeight = bs.layerHeight.value;
        bs.buildInLayers.value = true;
        bs.layerOrder.value = false; // bottom up
        bs.layerHeight.value = 1;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (outcome != Outcome.NONE || scanner == null) return null;
        IBuilderProcess builder = mod.getClientBaritone().getBuilderProcess();
        boolean passDone = scanner.tick(mod.getWorld());
        if (!scanner.ready()) {
            setDebugState("Checking what is already built...");
            return null;
        }
        job.total = scanner.total();
        job.placed = scanner.done();
        if (passDone && scanner.complete()) {
            if (builder.isActive()) builder.onLostControl();
            outcome = Outcome.DONE;
            return null;
        }

        // Anything still to place that we don't carry?
        List<ItemTarget> toGet = new ArrayList<>();
        for (Map.Entry<String, Integer> e : scanner.missingItems().entrySet()) {
            Item item = Registry.ITEM.get(new Identifier(e.getKey()));
            if (item == Items.AIR) continue;
            int have = mod.getItemStorage().getItemCount(item);
            if (have >= e.getValue()) continue;
            if (!TaskCatalogue.taskExists(item)) {
                job.pendingSkip = blocksNeeding(e.getKey());
                waitFor("Can't get " + e.getKey().replace("minecraft:", "") + " x" + (e.getValue() - have)
                        + ". Put some in your inventory and press Resume, or skip that block.");
                return null;
            }
            toGet.add(new ItemTarget(item, e.getValue()));
        }
        if (!toGet.isEmpty()) {
            if (builder.isActive()) builder.onLostControl();
            // One material at a time, wood last: gathering several at once let AltoClef's planks task pull
            // planks back out of the 2x2 grid while its pickaxe task was crafting sticks there (endless loop).
            toGet.sort(java.util.Comparator.comparingInt(t -> isWood(t) ? 1 : 0));
            ItemTarget first = toGet.get(0);
            setDebugState("Getting materials: " + first + " (" + toGet.size() + " kinds left)");
            return TaskCatalogue.getItemTask(first);
        }

        if (builder.isPaused()) builder.onLostControl();
        if (!builder.isActive()) builder.build("altoclefplus " + job.name, placed, placed.origin());
        setDebugState("Building " + job.name + " (" + job.placed + "/" + job.total + ")");
        return null;
    }

    /** Block ids in this schematic that are placed from {@code itemId}. */
    private java.util.List<String> blocksNeeding(String itemId) {
        java.util.List<String> out = new ArrayList<>();
        for (String state : schematic.palette()) {
            Map<String, Integer> one = new java.util.TreeMap<>();
            Materials.addState(state, one, new java.util.TreeMap<>());
            String block = Schematic.blockId(state);
            if (one.containsKey(itemId) && !out.contains(block)) out.add(block);
        }
        return out;
    }

    private static boolean isWood(ItemTarget t) {
        for (Item i : t.getMatches()) {
            String id = Registry.ITEM.getId(i).getPath();
            if (id.endsWith("_planks") || id.endsWith("_log") || id.equals("stick")) return true;
        }
        return false;
    }

    private void waitFor(String reason) {
        outcome = Outcome.WAITING_APPROVAL;
        why = reason;
    }

    private void fail(String reason) {
        outcome = Outcome.FAILED;
        why = reason;
    }

    private static String describe(Map<String, Integer> blocks) {
        StringBuilder sb = new StringBuilder();
        blocks.forEach((b, n) -> sb.append(sb.length() > 0 ? ", " : "").append(b.replace("minecraft:", "")).append(" x").append(n));
        return sb.toString();
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        IBuilderProcess builder = mod.getClientBaritone().getBuilderProcess();
        if (builder.isActive()) builder.onLostControl();
        Settings bs = mod.getClientBaritoneSettings();
        if (savedLayers != null) bs.buildInLayers.value = savedLayers;
        if (savedLayerOrder != null) bs.layerOrder.value = savedLayerOrder;
        if (savedLayerHeight != null) bs.layerHeight.value = savedLayerHeight;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return outcome != Outcome.NONE;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BuildTask b && b.job == job;
    }

    @Override
    protected String toDebugString() {
        return "Building " + job.name;
    }
}
