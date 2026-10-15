package altoclefplus.build.logic;

/**
 * Where a schematic goes in the world: an origin block and a rotation in quarter turns clockwise (seen from
 * above), like Minecraft's BlockRotation. Schematic (x, y, z) -> world, rotating around the origin.
 */
public record Placement(int originX, int originY, int originZ, int rotation) {
    public Placement {
        rotation = Math.floorMod(rotation, 4);
    }

    public int[] toWorld(int x, int y, int z) {
        int rx, rz;
        switch (rotation) {
            case 1 -> { rx = -z; rz = x; }
            case 2 -> { rx = -x; rz = -z; }
            case 3 -> { rx = z; rz = -x; }
            default -> { rx = x; rz = z; }
        }
        return new int[]{originX + rx, originY + y, originZ + rz};
    }

    /** {minX, minY, minZ, maxX, maxY, maxZ} of a schematic of this size placed here. */
    public int[] bounds(int sizeX, int sizeY, int sizeZ) {
        int[] a = toWorld(0, 0, 0);
        int[] b = toWorld(sizeX - 1, sizeY - 1, sizeZ - 1);
        return new int[]{Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]),
                Math.max(a[0], b[0]), Math.max(a[1], b[1]), Math.max(a[2], b[2])};
    }

    /** The schematic's +Z side points the way you look: south = no turn, west = 1, north = 2, east = 3. */
    public static int rotationForFacing(String facing) {
        return switch (facing) {
            case "west" -> 1;
            case "north" -> 2;
            case "east" -> 3;
            default -> 0;
        };
    }

    public Placement moved(int dx, int dy, int dz) {
        return new Placement(originX + dx, originY + dy, originZ + dz, rotation);
    }

    public Placement rotated(int quarterTurns) {
        return new Placement(originX, originY, originZ, rotation + quarterTurns);
    }
}
