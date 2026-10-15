package altoclefplus.ui;

import altoclefplus.Plus;
import altoclefplus.zones.Zones;
import altoclefplus.zones.logic.Zone;
import altoclefplus.zones.logic.ZoneRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Zones tab: list of this world's safe zones, create one from two corners at your position, and switch
 * each zone's rules. Corners are kept while the game runs so you can walk between them.
 */
final class ZonesTab {
    private static BlockPos corner1;
    private static BlockPos corner2;
    private static boolean fullHeight = true;
    private static int selected = -1;
    private static String nameText = "";

    private final Consumer<ClickableWidget> add;
    private final Runnable rebuild;
    private TextFieldWidget name;
    /** Top of the zone list / rules area (below the corner hint and the selected-zone title). */
    private static final int LIST_OFFSET = 86;

    ZonesTab(Consumer<ClickableWidget> add, Runnable rebuild) {
        this.add = add;
        this.rebuild = rebuild;
    }

    private static List<Zone> mine() {
        return Zones.current().zones();
    }

    void init(TextRenderer tr, int x, int y, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean inWorld = client.player != null;

        // New zone row
        name = new TextFieldWidget(tr, x + 40, y, 110, 18, new LiteralText("Zone name"));
        name.setText(nameText.isEmpty() ? "Zone " + (mine().size() + 1) : nameText);
        name.setChangedListener(t -> nameText = t);
        add.accept(name);
        ButtonWidget create = new ButtonWidget(x + 156, y - 1, 70, 20, new LiteralText("Create"), b -> create());
        int y2 = y + 23;
        ButtonWidget c1 = new ButtonWidget(x, y2, 70, 20, new LiteralText("Corner 1"), b -> {
            corner1 = client.player.getBlockPos();
            rebuild.run();
        });
        ButtonWidget c2 = new ButtonWidget(x + 74, y2, 70, 20, new LiteralText("Corner 2"), b -> {
            corner2 = client.player.getBlockPos();
            rebuild.run();
        });
        ButtonWidget full = new ButtonWidget(x + 148, y2, 90, 20, new LiteralText(fullHeight ? "Full height" : "Exact height"), b -> {
            fullHeight = !fullHeight;
            b.setMessage(new LiteralText(fullHeight ? "Full height" : "Exact height"));
        });
        c1.active = inWorld;
        c2.active = inWorld;
        create.active = inWorld && corner1 != null && corner2 != null;
        add.accept(c1);
        add.accept(c2);
        add.accept(full);
        add.accept(create);

        // Zone list (left) and rules of the selected zone (right)
        int listY = y + LIST_OFFSET;
        List<Zone> zones = mine();
        if (selected >= zones.size()) selected = zones.size() - 1;
        for (int i = 0; i < zones.size() && listY + i * 22 < y + h - 24; i++) {
            int idx = i;
            ButtonWidget pick = new ButtonWidget(x, listY + i * 22, 150, 20, new LiteralText(shorten(zones.get(i).name, 20)), b -> {
                selected = idx;
                rebuild.run();
            });
            pick.active = i != selected;
            add.accept(pick);
        }
        if (selected >= 0) {
            Zone z = zones.get(selected);
            ZoneRules r = z.rules();
            int rx = x + 170;
            // Two columns of rules when there is room, so the list doesn't run off the screen.
            int colW = (w - 170) >= 2 * 176 ? 172 : Math.min(210, w - 170);
            boolean two = colW == 172;
            java.util.function.IntUnaryOperator colX = n -> two && n >= 4 ? rx + colW + 4 : rx;
            java.util.function.IntUnaryOperator rowY = n -> listY + (two ? n % 4 : n) * 22;
            String[] labels = {"No breaking blocks", "No placing blocks", "No looting chests", "No entering",
                    "No attacking hostile", "No attacking neutral", "No attacking friendly"};
            BooleanSupplier[] gets = {() -> r.noBreak, () -> r.noPlace, () -> r.noLoot, () -> r.noEnter,
                    () -> r.noAttackHostile, () -> r.noAttackNeutral, () -> r.noAttackFriendly};
            List<Consumer<Boolean>> sets = List.of(v -> r.noBreak = v, v -> r.noPlace = v, v -> r.noLoot = v,
                    v -> r.noEnter = v, v -> r.noAttackHostile = v, v -> r.noAttackNeutral = v, v -> r.noAttackFriendly = v);
            for (int i = 0; i < labels.length; i++) {
                toggle(colX.applyAsInt(i), rowY.applyAsInt(i), colW, labels[i], gets[i], sets.get(i));
            }
            int deleteY = two ? listY + 4 * 22 + 4 : listY + labels.length * 22 + 4;
            add.accept(new ButtonWidget(rx, deleteY, 80, 20, new LiteralText("§cDelete"), b -> {
                Plus.config().zones.remove(z);
                selected = -1;
                saveAndRefresh();
            }));
        }
    }

    private int toggle(int x, int y, int width, String label, BooleanSupplier get, Consumer<Boolean> set) {
        add.accept(new ButtonWidget(x, y, width, 20, label(label, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(label, get.getAsBoolean()));
            saveAndRefresh();
        }));
        return y + 22;
    }

    private static Text label(String text, boolean on) {
        return new LiteralText((on ? "§a✔ " : "§7✖ ") + text);
    }

    private void create() {
        String world = Zones.worldKey();
        if (corner1 == null || corner2 == null || world.isEmpty()) return;
        String n = name.getText().isBlank() ? "Zone" : name.getText().trim();
        Zone z = Zone.fromCorners(n, world, corner1.getX(), corner1.getY(), corner1.getZ(),
                corner2.getX(), corner2.getY(), corner2.getZ(), fullHeight);
        Plus.config().zones.add(z);
        corner1 = null;
        corner2 = null;
        nameText = "";
        saveAndRefresh();
        selected = mine().size() - 1;
        rebuild.run();
    }

    private void saveAndRefresh() {
        Plus.saveConfig();
        Zones.invalidate();
        Zones.refresh();
    }

    void render(MatrixStack m, TextRenderer tr, int x, int y) {
        tr.drawWithShadow(m, "Name:", x, y + 5, 0xFFFFFF);
        String c1 = corner1 == null ? "-" : corner1.toShortString();
        String c2 = corner2 == null ? "-" : corner2.toShortString();
        tr.drawWithShadow(m, "Corner 1: " + c1 + "   Corner 2: " + c2, x, y + 48, 0xAAAAAA);
        tr.drawWithShadow(m, "Stand on a corner and press its button.", x, y + 58, 0x777777);
        List<Zone> zones = mine();
        if (zones.isEmpty()) {
            tr.drawWithShadow(m, "No zones in this world yet.", x, y + LIST_OFFSET, 0xAAAAAA);
        } else if (selected >= 0 && selected < zones.size()) {
            Zone z = zones.get(selected);
            tr.drawWithShadow(m, z.name + " - " + z.size() + " blocks", x + 170, y + LIST_OFFSET - 12, 0x55FF55);
        }
    }

    void tick() {
        if (name != null) name.tick();
    }

    private static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
