package altoclefplus.build;

import altoclefplus.build.logic.Materials;
import altoclefplus.build.logic.Schematic;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Compares the world with the schematic a slice at a time (so big builds never stall a tick): how many
 * blocks are already right, how many are left to place or clear, and which items the rest still needs.
 */
public final class BuildScanner {
    private static final int PER_TICK = 20_000;

    private final PlacedSchematic placed;
    private final int sx, sy, sz, volume;
    private int cursor;
    // Counts of the pass in progress...
    private int curTotal, curDone, curClear;
    private final Map<String, Integer> curMissingStates = new HashMap<>();
    // ...and of the last finished pass.
    private boolean ready;
    private int total, done, toClear;
    private Map<String, Integer> missingItems = Map.of();
    private Map<String, Integer> impossible = Map.of();

    public BuildScanner(PlacedSchematic placed) {
        this.placed = placed;
        Schematic s = placed.schematic();
        sx = s.sizeX();
        sy = s.sizeY();
        sz = s.sizeZ();
        volume = sx * sy * sz;
    }

    /** Scans the next slice; returns true when a full pass just finished. */
    public boolean tick(World world) {
        Schematic s = placed.schematic();
        int end = Math.min(volume, cursor + PER_TICK);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int i = cursor; i < end; i++) {
            int c = s.cells()[i];
            if (c == Schematic.NONE) continue;
            int x = i % sx;
            int z = (i / sx) % sz;
            int y = i / (sx * sz);
            int[] w = placed.placement().toWorld(x, y, z);
            BlockState want = placed.desiredAt(w[0], w[1], w[2]);
            if (want == null) continue;
            pos.set(w[0], w[1], w[2]);
            if (!world.isChunkLoaded(pos)) {
                if (!want.isAir()) {
                    curTotal++;
                    curMissingStates.merge(s.palette()[c], 1, Integer::sum);
                }
                continue;
            }
            BlockState now = world.getBlockState(pos);
            if (want.isAir()) {
                if (!now.isAir() && now.getFluidState().isEmpty()) curClear++;
                continue;
            }
            curTotal++;
            if (now.getBlock() == want.getBlock()) curDone++;
            else curMissingStates.merge(s.palette()[c], 1, Integer::sum);
        }
        cursor = end;
        if (cursor < volume) return false;
        // Pass finished: publish and start over.
        Map<String, Integer> items = new TreeMap<>();
        Map<String, Integer> imp = new TreeMap<>();
        curMissingStates.forEach((state, n) -> {
            Map<String, Integer> one = new TreeMap<>();
            Materials.addState(state, one, imp);
            one.forEach((item, k) -> items.merge(item, k * n, Integer::sum));
        });
        total = curTotal;
        done = curDone;
        toClear = curClear;
        missingItems = items;
        impossible = imp;
        ready = true;
        cursor = 0;
        curTotal = curDone = curClear = 0;
        curMissingStates.clear();
        return true;
    }

    public boolean ready() {
        return ready;
    }

    public int total() {
        return total;
    }

    public int done() {
        return done;
    }

    public int toClear() {
        return toClear;
    }

    public boolean complete() {
        return ready && done >= total && toClear == 0;
    }

    /** Items for the blocks not placed yet (before subtracting what is carried). */
    public Map<String, Integer> missingItems() {
        return missingItems;
    }
}
