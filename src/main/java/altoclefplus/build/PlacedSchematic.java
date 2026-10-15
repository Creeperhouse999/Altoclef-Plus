package altoclefplus.build;

import altoclefplus.build.logic.Materials;
import altoclefplus.build.logic.Placement;
import altoclefplus.build.logic.Schematic;
import baritone.api.schematic.ISchematic;
import com.mojang.brigadier.StringReader;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

import java.util.Set;
import java.util.function.Predicate;

/**
 * A schematic placed and rotated in the world, as Baritone's builder sees it: coordinates relative to the
 * placement's minimum corner. It also enforces our rules, which the builder itself ignores: nothing inside a
 * no-break/no-place safe zone or next to an item frame is touched, chests and beds in the way are never
 * cleared, and survival-impossible or skipped blocks are left as they are.
 */
public final class PlacedSchematic implements ISchematic {
    private final Schematic schematic;
    private final Placement placement;
    private final int minX, minY, minZ, width, height, length;
    private final BlockState[] stateCache;
    private final boolean[] skip;
    private final Predicate<BlockPos> mayTouch;

    /**
     * @param skipBlocks block ids not to build (survival-impossible ones you approved skipping, items it can't get)
     * @param mayTouch   false where our rules forbid changing the world (safe zones, item frames)
     */
    public PlacedSchematic(Schematic schematic, Placement placement, Set<String> skipBlocks, Predicate<BlockPos> mayTouch) {
        this.schematic = schematic;
        this.placement = placement;
        int[] b = placement.bounds(schematic.sizeX(), schematic.sizeY(), schematic.sizeZ());
        minX = b[0];
        minY = b[1];
        minZ = b[2];
        width = b[3] - b[0] + 1;
        height = b[4] - b[1] + 1;
        length = b[5] - b[2] + 1;
        stateCache = new BlockState[schematic.palette().length];
        skip = new boolean[schematic.palette().length];
        BlockRotation rot = switch (placement.rotation()) {
            case 1 -> BlockRotation.CLOCKWISE_90;
            case 2 -> BlockRotation.CLOCKWISE_180;
            case 3 -> BlockRotation.COUNTERCLOCKWISE_90;
            default -> BlockRotation.NONE;
        };
        for (int i = 0; i < stateCache.length; i++) {
            String s = schematic.palette()[i];
            String block = Schematic.blockId(s);
            skip[i] = skipBlocks.contains(block) || Materials.SURVIVAL_IMPOSSIBLE.contains(block);
            BlockState parsed = parse(s);
            stateCache[i] = parsed == null ? null : parsed.rotate(rot);
            if (parsed == null) skip[i] = true; // unknown block (newer version): leave the world as is
        }
        this.mayTouch = mayTouch;
    }

    static BlockState parse(String state) {
        try {
            return new BlockArgumentParser(new StringReader(state), false).parse(false).getBlockState();
        } catch (Exception e) {
            return null;
        }
    }

    public BlockPos origin() {
        return new BlockPos(minX, minY, minZ);
    }

    /** The desired state at a world position, or null if the schematic doesn't decide there. */
    public BlockState desiredAt(int wx, int wy, int wz) {
        int[] s = placement.toSchematic(wx, wy, wz);
        if (!schematic.inBounds(s[0], s[1], s[2])) return null;
        int c = schematic.cells()[schematic.index(s[0], s[1], s[2])];
        if (c == Schematic.NONE || skip[c]) return null;
        return stateCache[c];
    }

    @Override
    public boolean inSchematic(int x, int y, int z, BlockState current) {
        return x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < length
                && desiredAt(minX + x, minY + y, minZ + z) != null;
    }

    @Override
    public BlockState desiredState(int x, int y, int z, BlockState current, java.util.List<BlockState> approxPlaceable) {
        BlockState want = desiredAt(minX + x, minY + y, minZ + z);
        if (want == null) return current;
        BlockPos pos = new BlockPos(minX + x, minY + y, minZ + z);
        if (!mayTouch.test(pos)) return current;
        // Never clear chests, shulker boxes or beds that are in the way.
        if (want.isAir() && current != null && (current.getBlock() instanceof ChestBlock
                || current.getBlock() instanceof ShulkerBoxBlock || current.getBlock() instanceof BedBlock
                || current.isOf(Blocks.BARREL))) {
            return current;
        }
        return want;
    }

    @Override
    public int widthX() {
        return width;
    }

    @Override
    public int heightY() {
        return height;
    }

    @Override
    public int lengthZ() {
        return length;
    }

    public Schematic schematic() {
        return schematic;
    }

    public Placement placement() {
        return placement;
    }
}
