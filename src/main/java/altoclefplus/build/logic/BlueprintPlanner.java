package altoclefplus.build.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Litematica-style blueprint: which schematic blocks to draw as ghosts. Blocks still missing are ghosts, a
 * different block in the way is marked wrong, and correctly placed blocks are not drawn (block states such as
 * stair facing are ignored). Only blocks near the camera, closest first, up to a cap.
 */
public final class BlueprintPlanner {
    public enum Kind { MISSING, WRONG }

    public record Ghost(int x, int y, int z, Kind kind) {}

    /** Block id ("minecraft:stone") at a world position. */
    @FunctionalInterface
    public interface WorldView {
        String blockAt(int x, int y, int z);
    }

    private BlueprintPlanner() {}

    public static List<Ghost> plan(Schematic s, Placement p, WorldView world, double camX, double camY, double camZ,
                                   int radius, int cap) {
        List<Ghost> out = new ArrayList<>();
        double r2 = (double) radius * radius;
        for (int y = 0; y < s.sizeY(); y++) {
            for (int z = 0; z < s.sizeZ(); z++) {
                for (int x = 0; x < s.sizeX(); x++) {
                    String want = s.stateAt(x, y, z);
                    if (want == null || Schematic.isAir(want)) continue;
                    int[] w = p.toWorld(x, y, z);
                    double dx = w[0] + 0.5 - camX, dy = w[1] + 0.5 - camY, dz = w[2] + 0.5 - camZ;
                    if (dx * dx + dy * dy + dz * dz > r2) continue;
                    String have = world.blockAt(w[0], w[1], w[2]);
                    if (have.equals(Schematic.blockId(want))) continue;
                    boolean empty = have.equals("minecraft:air") || have.equals("minecraft:cave_air") || have.equals("minecraft:void_air");
                    out.add(new Ghost(w[0], w[1], w[2], empty ? Kind.MISSING : Kind.WRONG));
                }
            }
        }
        out.sort(Comparator.comparingDouble(g -> {
            double dx = g.x() + 0.5 - camX, dy = g.y() + 0.5 - camY, dz = g.z() + 0.5 - camZ;
            return dx * dx + dy * dy + dz * dz;
        }));
        return out.size() > cap ? new ArrayList<>(out.subList(0, cap)) : out;
    }
}
