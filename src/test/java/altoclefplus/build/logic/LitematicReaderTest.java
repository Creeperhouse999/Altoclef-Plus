package altoclefplus.build.logic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class LitematicReaderTest {
    /** Packs palette indexes like Litematica's bit array (values may span two longs). */
    static long[] pack(int[] values, int bits) {
        long[] arr = new long[(int) Math.ceil(values.length * bits / 64.0)];
        long mask = (1L << bits) - 1;
        for (int i = 0; i < values.length; i++) {
            long v = values[i] & mask;
            int start = i * bits;
            int si = start >> 6;
            int ei = ((i + 1) * bits - 1) >> 6;
            int off = start & 63;
            arr[si] = arr[si] & ~(mask << off) | v << off;
            if (si != ei) {
                int endOff = 64 - off;
                int j1 = bits - endOff;
                arr[ei] = arr[ei] >>> j1 << j1 | v >> endOff;
            }
        }
        return arr;
    }

    static NbtCompound state(String name, String... props) {
        NbtCompound c = new NbtCompound();
        c.putString("Name", name);
        if (props.length > 0) {
            NbtCompound p = new NbtCompound();
            for (int i = 0; i < props.length; i += 2) p.putString(props[i], props[i + 1]);
            c.put("Properties", p);
        }
        return c;
    }

    static NbtCompound vec(int x, int y, int z) {
        NbtCompound c = new NbtCompound();
        c.putInt("x", x);
        c.putInt("y", y);
        c.putInt("z", z);
        return c;
    }

    static NbtCompound region(int px, int py, int pz, int sx, int sy, int sz, NbtCompound[] palette, int[] values) {
        NbtCompound r = new NbtCompound();
        r.put("Position", vec(px, py, pz));
        r.put("Size", vec(sx, sy, sz));
        NbtList pal = new NbtList();
        for (NbtCompound s : palette) pal.add(s);
        r.put("BlockStatePalette", pal);
        int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(palette.length - 1));
        r.putLongArray("BlockStates", pack(values, bits));
        r.put("TileEntities", new NbtList());
        r.put("Entities", new NbtList());
        return r;
    }

    static byte[] file(NbtCompound regions) throws IOException {
        NbtCompound root = new NbtCompound();
        root.putInt("Version", 6);
        NbtCompound meta = new NbtCompound();
        meta.putString("Name", "Test hut");
        meta.putString("Author", "Creeperhouse999");
        root.put("Metadata", meta);
        root.put("Regions", regions);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root, out);
        return out.toByteArray();
    }

    @Test
    void readsASingleRegion() throws IOException {
        NbtCompound[] palette = {state("minecraft:air"), state("minecraft:stone"),
                state("minecraft:oak_stairs", "half", "bottom", "facing", "north")};
        // index = y * (sx * sz) + z * sx + x, size 2x2x2
        int[] values = {1, 1, 1, 1, 0, 2, 0, 0};
        NbtCompound regions = new NbtCompound();
        regions.put("Main", region(0, 0, 0, 2, 2, 2, palette, values));

        Schematic s = LitematicReader.read(new ByteArrayInputStream(file(regions)));
        assertEquals("Test hut", s.name());
        assertEquals("Creeperhouse999", s.author());
        assertEquals(2, s.sizeX());
        assertEquals(2, s.sizeY());
        assertEquals(2, s.sizeZ());
        assertEquals("minecraft:stone", s.stateAt(1, 0, 1));
        assertEquals("minecraft:air", s.stateAt(0, 1, 0));
        assertEquals("minecraft:oak_stairs[facing=north,half=bottom]", s.stateAt(1, 1, 0), "properties sorted");
        assertEquals(5, s.nonAirCount());
    }

    @Test
    void handlesNegativeSizesAndSeveralRegions() throws IOException {
        NbtCompound[] palette = {state("minecraft:air"), state("minecraft:dirt")};
        NbtCompound regions = new NbtCompound();
        // Size -2 on x: the region spans x = -1..0 relative to its position.
        regions.put("A", region(0, 0, 0, -2, 1, 1, palette, new int[]{1, 1}));
        regions.put("B", region(2, 0, 0, 1, 1, 1, palette, new int[]{1}));
        Schematic s = LitematicReader.read(new ByteArrayInputStream(file(regions)));
        assertEquals(4, s.sizeX(), "x from -1 to 2");
        assertEquals("minecraft:dirt", s.stateAt(0, 0, 0));
        assertEquals("minecraft:dirt", s.stateAt(1, 0, 0));
        assertNull(s.stateAt(2, 0, 0), "between regions: not part of the schematic (world left as is)");
        assertEquals("minecraft:dirt", s.stateAt(3, 0, 0));
    }

    @Test
    void valuesThatSpanTwoLongs() throws IOException {
        NbtCompound[] palette = {state("minecraft:air"), state("minecraft:stone"), state("minecraft:dirt"),
                state("minecraft:sand"), state("minecraft:glass")};
        int n = 5 * 1 * 5; // 3 bits per value -> crosses long boundaries
        int[] values = new int[n];
        for (int i = 0; i < n; i++) values[i] = i % 5;
        NbtCompound regions = new NbtCompound();
        regions.put("R", region(0, 0, 0, 5, 1, 5, palette, values));
        Schematic s = LitematicReader.read(new ByteArrayInputStream(file(regions)));
        for (int i = 0; i < n; i++) {
            int x = i % 5, z = i / 5;
            String want = palette[i % 5].getString("Name");
            assertEquals(want, s.stateAt(x, 0, z), "index " + i);
        }
    }
}
