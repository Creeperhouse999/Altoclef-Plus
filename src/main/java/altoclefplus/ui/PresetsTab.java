package altoclefplus.ui;

import altoclefplus.Plus;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.ui.logic.Kit;
import altoclefplus.ui.logic.PresetLibrary;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Presets tab: your own presets and a built-in library (tools, armor, kits, resources, food, speedrun), click to
 * run. Shift-click one of yours to delete it, Ctrl-click it to edit it as a kit. "Make a kit" opens the kit
 * builder: pick items and amounts, name it, save it as a preset or run it right away.
 */
final class PresetsTab {
    private static final int ROW = 24;
    private static final int TEXT = 0xFFFFFF;
    private static final int MUTED = 0xAAAAAA;
    private static final int HEADING = 0xFFD37F;

    private static boolean kitMode;
    private static int scroll;
    private static List<Kit.Entry> kit = new ArrayList<>();
    private static String kitName = "";
    private static String selectedItem = "";

    private final Consumer<ClickableWidget> add;
    private final Runnable rebuild;
    private final Consumer<String> run;
    private final boolean game;
    /** Category headings to draw: label and y. */
    private final List<Object[]> headings = new ArrayList<>();
    private ItemGrid grid;
    private TextFieldWidget nameField;
    private TextFieldWidget amountField;
    private int x, y, w, h;
    private int contentRows;
    private int visibleRows;

    PresetsTab(Consumer<ClickableWidget> add, Runnable rebuild, Consumer<String> run, boolean game) {
        this.add = add;
        this.rebuild = rebuild;
        this.run = run;
        this.game = game;
    }

    void init(TextRenderer tr, int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        headings.clear();
        grid = null;
        if (kitMode) initKit(tr);
        else initList(tr);
    }

    // ---- list of presets ----
    private void initList(TextRenderer tr) {
        add.accept(new ButtonWidget(x, y, 100, 20, new LiteralText("Make a kit"), b -> {
            kitMode = true;
            kit = new ArrayList<>();
            kitName = "";
            rebuild.run();
        }));
        int top = y + 26;
        int bottom = y + h - 4;
        // Lay everything out in rows, then only add the rows that fit after scrolling.
        List<Row> rows = new ArrayList<>();
        List<PlusConfig.Preset> mine = Plus.config().ui.presets;
        if (!mine.isEmpty()) {
            rows.add(Row.heading("My presets (shift-click: delete, ctrl-click: edit)"));
            flow(tr, rows, mine.stream().map(p -> new Item(p.name, p.command, p)).toList());
        }
        for (PresetLibrary.Category c : PresetLibrary.all()) {
            rows.add(Row.heading(c.name()));
            flow(tr, rows, c.presets().stream().map(p -> new Item(p.name(), p.command(), null)).toList());
        }
        contentRows = rows.size();
        int visible = Math.max(1, (bottom - top) / ROW);
        visibleRows = visible;
        scroll = Math.max(0, Math.min(scroll, contentRows - visible));
        for (int i = scroll; i < Math.min(rows.size(), scroll + visible); i++) {
            Row r = rows.get(i);
            int ry = top + (i - scroll) * ROW;
            if (r.heading != null) {
                headings.add(new Object[]{r.heading, ry + 8});
                continue;
            }
            int px = x;
            for (Item it : r.items) {
                int bw = tr.getWidth(it.name) + 12;
                ButtonWidget b = new ButtonWidget(px, ry, bw, 20, new LiteralText(it.name), btn -> click(it));
                b.active = game || it.mine != null;
                add.accept(b);
                px += bw + 4;
            }
        }
    }

    private void flow(TextRenderer tr, List<Row> rows, List<Item> items) {
        List<Item> line = new ArrayList<>();
        int used = 0;
        for (Item it : items) {
            int bw = tr.getWidth(it.name) + 16;
            if (!line.isEmpty() && used + bw > w) {
                rows.add(Row.items(line));
                line = new ArrayList<>();
                used = 0;
            }
            line.add(it);
            used += bw;
        }
        if (!line.isEmpty()) rows.add(Row.items(line));
    }

    private void click(Item it) {
        if (it.mine != null && Screen.hasShiftDown()) {
            Plus.config().ui.presets.remove(it.mine);
            Plus.saveConfig();
            Plus.log().add("plus", "Removed preset " + it.name);
            rebuild.run();
            return;
        }
        if (it.mine != null && Screen.hasControlDown()) {
            kit = new ArrayList<>(Kit.parse(it.command));
            kitName = it.name;
            kitMode = true;
            rebuild.run();
            return;
        }
        if (game) run.accept(it.command);
    }

    // ---- kit builder ----
    private void initKit(TextRenderer tr) {
        add.accept(new ButtonWidget(x, y, 60, 20, new LiteralText("< Back"), b -> {
            kitMode = false;
            rebuild.run();
        }));
        nameField = new TextFieldWidget(tr, x + 110, y + 1, 140, 18, new LiteralText("Kit name"));
        nameField.setMaxLength(24);
        nameField.setText(kitName);
        nameField.setChangedListener(t -> kitName = t);
        add.accept(nameField);
        ButtonWidget save = new ButtonWidget(x + 256, y, 60, 20, new LiteralText("Save"), b -> saveKit());
        save.active = !kit.isEmpty();
        add.accept(save);
        ButtonWidget runKit = new ButtonWidget(x + 320, y, 60, 20, new LiteralText("Run"), b -> run.accept(Kit.command(kit)));
        runKit.active = game && !kit.isEmpty();
        add.accept(runKit);

        int sy = y + 26;
        TextFieldWidget search = new TextFieldWidget(tr, x + 52, sy, 110, 18, new LiteralText("Search items"));
        search.setChangedListener(t -> grid.setFilter(t));
        add.accept(search);
        amountField = new TextFieldWidget(tr, x + 186, sy, 36, 18, new LiteralText("Amount"));
        amountField.setText("1");
        amountField.setTextPredicate(t -> t.matches("\\d{0,4}"));
        add.accept(amountField);
        add.accept(new ButtonWidget(x + 226, sy - 1, 40, 20, new LiteralText("+1"), b -> addSelected(1)));
        add.accept(new ButtonWidget(x + 268, sy - 1, 40, 20, new LiteralText("+16"), b -> addSelected(16)));
        add.accept(new ButtonWidget(x + 310, sy - 1, 40, 20, new LiteralText("+64"), b -> addSelected(64)));
        add.accept(new ButtonWidget(x + 352, sy - 1, 40, 20, new LiteralText("Add"), b -> addSelected(amount())));

        // Left: the item grid; right: what is in the kit (click an entry's "-" to take it out).
        int listW = Math.min(200, w / 3);
        int gridTop = sy + 24;
        grid = new ItemGrid(id -> selectedItem = id, (Predicate<String>) id -> id.equals(selectedItem));
        grid.setBounds(x, gridTop, w - listW - 10, Math.max(18, y + h - gridTop - 4));
        int lx = x + w - listW;
        int ly = gridTop;
        for (Kit.Entry e : kit) {
            if (ly + 20 > y + h) break;
            add.accept(new ButtonWidget(lx, ly, 20, 20, new LiteralText("-"), b -> {
                kit = Kit.remove(kit, e.item(), Screen.hasShiftDown() ? e.count() : 1);
                rebuild.run();
            }));
            ly += 22;
        }
    }

    private int amount() {
        String t = amountField == null ? "" : amountField.getText();
        return t.isEmpty() ? 1 : Math.max(1, Integer.parseInt(t));
    }

    private void addSelected(int n) {
        if (selectedItem.isEmpty()) return;
        // One item AltoClef can't fetch would make the whole "get [...]" command fail.
        if (!adris.altoclef.TaskCatalogue.taskExists(selectedItem)) {
            Plus.log().add("plus", "AltoClef can't get " + selectedItem.replace('_', ' ') + " - not added to the kit");
            return;
        }
        kit = Kit.add(kit, selectedItem, n);
        rebuild.run();
    }

    private void saveKit() {
        if (kit.isEmpty()) return;
        String name = kitName.isBlank() ? "My kit" : kitName.trim();
        String command = Kit.command(kit);
        // Saving under an existing name replaces that preset (that is how editing works).
        Plus.config().ui.presets.removeIf(p -> p.name.equals(name));
        Plus.config().ui.presets.add(new PlusConfig.Preset(name, command));
        Plus.saveConfig();
        Plus.log().add("plus", "Saved kit " + name);
        kitMode = false;
        rebuild.run();
    }

    // ---- rendering and input ----
    void render(MatrixStack m, TextRenderer tr, int mouseX, int mouseY) {
        if (!kitMode) {
            for (Object[] hd : headings) tr.drawWithShadow(m, (String) hd[0], x, (int) hd[1] - 4, HEADING);
            if (contentRows > visibleRows) tr.drawWithShadow(m, "Scroll for more", x + 110, y + 6, MUTED);
            return;
        }
        tr.drawWithShadow(m, "Name:", x + 76, y + 6, TEXT);
        tr.drawWithShadow(m, "Item:", x, y + 31, TEXT);
        tr.drawWithShadow(m, "x", x + 176, y + 31, MUTED);
        int listW = Math.min(200, w / 3);
        int lx = x + w - listW;
        int ly = y + 50;
        if (kit.isEmpty()) tr.drawWithShadow(m, "Pick an item, then +1 / +16 / +64", lx, ly + 6, MUTED);
        for (Kit.Entry e : kit) {
            if (ly + 20 > y + h) break;
            tr.drawWithShadow(m, e.count() + " x " + e.item().replace('_', ' '), lx + 24, ly + 6, TEXT);
            ly += 22;
        }
        if (!selectedItem.isEmpty()) tr.drawWithShadow(m, selectedItem.replace('_', ' '), x + 52, y + 46, MUTED);
        if (grid != null) grid.render(m, mouseX, mouseY);
    }

    boolean mouseClicked(double mx, double my) {
        return kitMode && grid != null && grid.mouseClicked(mx, my);
    }

    boolean mouseScrolled(double mx, double my, double amount) {
        if (kitMode) return grid != null && grid.mouseScrolled(mx, my, amount);
        int before = scroll;
        scroll = Math.max(0, scroll - (int) Math.signum(amount));
        if (scroll != before || amount < 0) rebuild.run();
        return true;
    }

    private record Item(String name, String command, PlusConfig.Preset mine) {}

    private static final class Row {
        String heading;
        List<Item> items;

        static Row heading(String text) {
            Row r = new Row();
            r.heading = text;
            return r;
        }

        static Row items(List<Item> items) {
            Row r = new Row();
            r.items = items;
            return r;
        }
    }
}
