package altoclefplus.zones.logic;

/** A named box in one world (optionally covering the full height) with its own rules. Gson-friendly. */
public final class Zone {
    public String name = "Zone";
    /** World key: singleplayer save name or server address. */
    public String world = "";
    public int minX, minY, minZ, maxX, maxY, maxZ;
    public boolean fullHeight;
    public ZoneRules rules = new ZoneRules();

    public static Zone fromCorners(String name, String world, int x1, int y1, int z1, int x2, int y2, int z2,
                                   boolean fullHeight) {
        Zone z = new Zone();
        z.name = name;
        z.world = world;
        z.minX = Math.min(x1, x2);
        z.maxX = Math.max(x1, x2);
        z.minY = Math.min(y1, y2);
        z.maxY = Math.max(y1, y2);
        z.minZ = Math.min(z1, z2);
        z.maxZ = Math.max(z1, z2);
        z.fullHeight = fullHeight;
        return z;
    }

    public boolean contains(int x, int y, int z) {
        boolean inY = fullHeight || (y >= minY && y <= maxY);
        return inY && x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean contains(double x, double y, double z) {
        return contains((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public ZoneRules rules() {
        if (rules == null) rules = new ZoneRules();
        return rules;
    }

    public String size() {
        return (maxX - minX + 1) + " x " + (fullHeight ? "all" : String.valueOf(maxY - minY + 1)) + " x " + (maxZ - minZ + 1);
    }
}
