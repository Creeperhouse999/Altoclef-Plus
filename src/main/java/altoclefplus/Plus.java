package altoclefplus;

import adris.altoclef.AltoClef;

/** Static service holder for the mod. */
public final class Plus {
    private static AltoClef mod;

    private Plus() {}

    public static void init(AltoClef altoClef) {
        mod = altoClef;
    }

    public static AltoClef mod() {
        return mod;
    }
}
