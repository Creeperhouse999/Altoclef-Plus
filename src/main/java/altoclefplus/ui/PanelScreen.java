package altoclefplus.ui;

import adris.altoclef.AltoClef;
import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.core.logic.LogBuffer;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.ui.logic.BotState;
import altoclefplus.ui.logic.PlanTracker;
import altoclefplus.ui.logic.StepTrail;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The control panel: Tasks, Plan, Status and Settings tabs, with Stop / Pause always visible. */
public final class PanelScreen extends PlusScreen {
    public enum Tab { TASKS("Tasks"), PLAN("Plan"), STATUS("Status"), ZONES("Zones"), SETTINGS("Settings");
        final String label;
        Tab(String label) { this.label = label; }
    }

    private static final int SIDEBAR_W = 70;
    private static final int ROW_H = 22;
    private static final int TEXT = 0xFFFFFF;
    private static final int MUTED = 0xAAAAAA;

    private static Tab lastTab = Tab.TASKS;

    private final Screen parent;
    private Tab tab;
    private int settingsPage;
    private int logScroll;

    private ItemGrid grid;
    private TextFieldWidget search;
    private TextFieldWidget count;
    private TextFieldWidget command;
    private ButtonWidget pauseButton;
    private String selectedItem = "";
    private ZonesTab zonesTab;

    public PanelScreen() {
        this(null, lastTab);
    }

    public PanelScreen(Screen parent, Tab tab) {
        super(new LiteralText("AltoClef Plus"));
        this.parent = parent;
        this.tab = tab;
    }

    // ---- layout helpers ----
    private int cx() { return 10 + SIDEBAR_W + 10; }
    private int cy() { return 28; }
    private int cw() { return width - cx() - 10; }
    private int bottomY() { return height - 26; }
    private int ch() { return bottomY() - cy() - 6; }

    private static boolean inGame() {
        return Plus.mod() != null && AltoClef.inGame();
    }

    @Override
    protected void init() {
        int y = cy();
        for (Tab t : Tab.values()) {
            ButtonWidget b = addDrawableChild(new ButtonWidget(10, y, SIDEBAR_W, 20, new LiteralText(t.label), btn -> switchTab(t)));
            b.active = t != tab;
            y += 24;
        }

        int by = bottomY();
        ButtonWidget stop = addDrawableChild(new ButtonWidget(10, by, 60, 20, new LiteralText("Stop"), b -> Plus.control().stop()));
        pauseButton = addDrawableChild(new ButtonWidget(74, by, 70, 20, pauseLabel(), b -> Plus.control().togglePause()));
        stop.active = inGame() && Plus.control() != null;
        pauseButton.active = stop.active;
        addDrawableChild(new ButtonWidget(width - 70, by, 60, 20, new LiteralText("Close"), b -> close()));

        switch (tab) {
            case TASKS -> initTasks();
            case SETTINGS -> initSettings();
            case ZONES -> {
                zonesTab = new ZonesTab(this::addDrawableChild, () -> {
                    clearChildren();
                    init();
                });
                zonesTab.init(textRenderer, cx(), cy(), cw(), ch());
            }
            default -> { }
        }
    }

    private void switchTab(Tab t) {
        tab = t;
        lastTab = t;
        settingsPage = 0;
        clearChildren();
        init();
    }

    private Text pauseLabel() {
        BotState st = Plus.control() == null ? BotState.IDLE : Plus.control().state();
        return new LiteralText(st == BotState.PAUSED ? "Resume" : "Pause");
    }

    // ---- Tasks tab ----
    private void initTasks() {
        boolean game = inGame();
        int x = cx();
        int y = cy();

        ButtonWidget gamer = addDrawableChild(new ButtonWidget(x, y, 110, 20, new LiteralText("Beat the game"), b -> run("gamer")));
        gamer.active = game;
        int px = x + 114;
        if (game && AltoClef.getCommandExecutor() != null && AltoClef.getCommandExecutor().get("huntendermen") != null) {
            addDrawableChild(new ButtonWidget(px, y, 100, 20, new LiteralText("Hunt endermen"), b -> run("huntendermen")));
            px += 104;
        }
        for (PlusConfig.Preset p : Plus.config().ui.presets) {
            int w = Math.max(50, textRenderer.getWidth(p.name) + 12);
            if (px + w > x + cw()) break;
            ButtonWidget pb = addDrawableChild(new ButtonWidget(px, y, w, 20, new LiteralText(p.name), b -> {
                if (hasShiftDown()) {
                    // Shift-click removes a preset.
                    Plus.config().ui.presets.remove(p);
                    Plus.saveConfig();
                    Plus.log().add("plus", "Removed preset " + p.name);
                    clearChildren();
                    init();
                } else {
                    run(p.command);
                }
            }));
            pb.active = game;
            px += w + 4;
        }

        y += 26;
        search = addDrawableChild(new TextFieldWidget(textRenderer, x + 52, y, 120, 18, new LiteralText("Search items")));
        search.setChangedListener(t -> grid.setFilter(t));
        count = addDrawableChild(new TextFieldWidget(textRenderer, x + 210, y, 36, 18, new LiteralText("Amount")));
        count.setText("1");
        count.setTextPredicate(t -> t.matches("\\d{0,4}"));
        ButtonWidget get = addDrawableChild(new ButtonWidget(x + 250, y - 1, 60, 20, new LiteralText("Get it"), b -> runGet()));
        get.active = game;

        int gridTop = y + 24;
        int gridBottom = bottomY() - 34;
        grid = new ItemGrid(id -> selectedItem = id, id -> id.equals(selectedItem));
        grid.setBounds(x, gridTop, cw(), Math.max(18, gridBottom - gridTop));

        int cyCmd = bottomY() - 28;
        command = addDrawableChild(new TextFieldWidget(textRenderer, x + 70, cyCmd, cw() - 164, 18, new LiteralText("Command")));
        command.setMaxLength(256);
        ButtonWidget runCmd = addDrawableChild(new ButtonWidget(x + cw() - 64, cyCmd - 1, 64, 20, new LiteralText("Run"), b -> {
            String c = command.getText().trim();
            if (!c.isEmpty()) run(c.startsWith("@") ? c.substring(1) : c);
        }));
        runCmd.active = game;
        // Save the typed command as a preset button (shift-click a preset to remove it).
        addDrawableChild(new ButtonWidget(x + cw() - 88, cyCmd - 1, 20, 20, new LiteralText("+"), b -> {
            String c = command.getText().trim();
            if (c.startsWith("@")) c = c.substring(1);
            if (c.isEmpty()) return;
            String name = altoclefplus.ui.logic.TaskNarrator.goal(c);
            if (name.length() > 18) name = name.substring(0, 17) + "…";
            Plus.config().ui.presets.add(new PlusConfig.Preset(name, c));
            Plus.saveConfig();
            Plus.log().add("plus", "Saved preset " + name + " (shift-click it to remove)");
            clearChildren();
            init();
        }));
    }

    private void runGet() {
        if (selectedItem.isEmpty()) return;
        String n = count.getText().isEmpty() ? "1" : count.getText();
        run("get " + selectedItem + " " + n);
    }

    private void run(String cmd) {
        if (!inGame()) return;
        Plus.control().start(cmd);
        close();
    }

    // ---- Settings tab ----
    private List<SettingsModel.Row> settingsRows() {
        return SettingsModel.rows(
                () -> client.setScreen(new ItemListScreen(this, "Protected items (never dropped)", Plus.config().inventory.protectedItems)),
                () -> client.setScreen(new ItemListScreen(this, "Junk items (may be dropped when full)", Plus.config().inventory.junkItems)),
                () -> client.setScreen(new HotbarLayoutScreen(this)),
                () -> client.setScreen(new EntityListScreen(this, "Never attack these mobs", Plus.config().rules.neverAttackMobs)),
                () -> client.setScreen(new ItemListScreen(this, "Never break these blocks", Plus.config().rules.neverBreakBlocks)));
    }

    private int rowsPerColumn() {
        return Math.max(3, (ch() - 26) / ROW_H);
    }

    /** Two columns of settings on wide screens, so there are far fewer pages. */
    private int settingsColumns() {
        return cw() >= 560 ? 2 : 1;
    }

    private int rowsPerPage() {
        return rowsPerColumn() * settingsColumns();
    }

    /** Left x, right x and top y of the n-th row on the current settings page. */
    private int[] settingsCell(int n) {
        int cols = settingsColumns();
        int gap = 16;
        int colW = (cw() - gap * (cols - 1)) / cols;
        int col = n / rowsPerColumn();
        int x = cx() + col * (colW + gap);
        return new int[]{x, x + colW, cy() + (n % rowsPerColumn()) * ROW_H};
    }

    private void initSettings() {
        List<SettingsModel.Row> rows = settingsRows();
        int perPage = rowsPerPage();
        int pages = Math.max(1, (rows.size() + perPage - 1) / perPage);
        settingsPage = Math.min(settingsPage, pages - 1);
        int start = settingsPage * perPage;
        for (int i = start; i < Math.min(rows.size(), start + perPage); i++) {
            SettingsModel.Row row = rows.get(i);
            int[] cell = settingsCell(i - start);
            int x = cell[0];
            int right = cell[1];
            int y = cell[2];
            if (row instanceof SettingsModel.Toggle t) {
                addDrawableChild(new ButtonWidget(right - 50, y, 50, 20, onOff(t.get().getAsBoolean()), b -> {
                    t.set().accept(!t.get().getAsBoolean());
                    b.setMessage(onOff(t.get().getAsBoolean()));
                }));
            } else if (row instanceof SettingsModel.Value v) {
                addDrawableChild(new ButtonWidget(right - 90, y, 20, 20, new LiteralText("-"), b -> {
                    v.set().accept(Math.max(v.min(), v.get().getAsInt() - v.step()));
                }));
                addDrawableChild(new ButtonWidget(right - 20, y, 20, 20, new LiteralText("+"), b -> {
                    v.set().accept(Math.min(v.max(), v.get().getAsInt() + v.step()));
                }));
            } else if (row instanceof SettingsModel.Action a) {
                addDrawableChild(new ButtonWidget(x, y, Math.min(200, right - x), 20, new LiteralText(a.label()), b -> a.run().run()));
            } else if (row instanceof SettingsModel.Choice ch) {
                addDrawableChild(new ButtonWidget(right - 130, y, 130, 20, new LiteralText(ch.currentLabel()), b -> {
                    ch.next();
                    b.setMessage(new LiteralText(ch.currentLabel()));
                }));
            }
        }
        int x = cx();
        int right = x + cw();
        int navY = bottomY() - 26;
        ButtonWidget prev = addDrawableChild(new ButtonWidget(x, navY, 20, 20, new LiteralText("<"), b -> {
            settingsPage--;
            clearChildren();
            init();
        }));
        prev.active = settingsPage > 0;
        ButtonWidget next = addDrawableChild(new ButtonWidget(x + 80, navY, 20, 20, new LiteralText(">"), b -> {
            settingsPage++;
            clearChildren();
            init();
        }));
        next.active = settingsPage < pages - 1;
        addDrawableChild(new ButtonWidget(right - 60, navY, 60, 20, new LiteralText("Save"), b -> {
            Plus.saveConfig();
            Plus.log().add("plus", "Settings saved");
        }));
    }

    /** Cut a label to fit {@code maxWidth} pixels, with an ellipsis. */
    private String fit(String text, int maxWidth) {
        if (maxWidth <= 0 || textRenderer.getWidth(text) <= maxWidth) return text;
        String t = text;
        while (t.length() > 1 && textRenderer.getWidth(t + "…") > maxWidth) t = t.substring(0, t.length() - 1);
        return t + "…";
    }

    private static Text onOff(boolean on) {
        return new LiteralText(on ? "§aON" : "§cOFF");
    }

    // ---- rendering ----
    @Override
    public void render(MatrixStack m, int mouseX, int mouseY, float delta) {
        renderBackground(m);
        drawCenteredText(m, textRenderer, title, width / 2, 10, TEXT);
        switch (tab) {
            case TASKS -> renderTasks(m, mouseX, mouseY);
            case PLAN -> renderPlan(m);
            case STATUS -> renderStatus(m);
            case ZONES -> {
                if (zonesTab != null) zonesTab.render(m, textRenderer, cx(), cy());
            }
            case SETTINGS -> renderSettings(m);
        }
        BotState st = Plus.control() == null ? BotState.IDLE : Plus.control().state();
        textRenderer.drawWithShadow(m, "State: " + st.label, 150, bottomY() + 6, MUTED);
        super.render(m, mouseX, mouseY, delta);
        if (tab == Tab.TASKS && grid != null) grid.render(m, mouseX, mouseY);
    }

    private void renderTasks(MatrixStack m, int mouseX, int mouseY) {
        int x = cx();
        int y = cy() + 26 + 5;
        textRenderer.drawWithShadow(m, "Item:", x, y, TEXT);
        textRenderer.drawWithShadow(m, "x", x + 198, y, MUTED);
        if (!selectedItem.isEmpty()) {
            textRenderer.drawWithShadow(m, selectedItem, x + 316, y, 0x55FF55);
        }
        textRenderer.drawWithShadow(m, "Command:", x, bottomY() - 23, TEXT);
        if (!inGame()) {
            textRenderer.drawWithShadow(m, "Join a world to run tasks.", x, bottomY() - 12, 0xFFAA00);
        }
    }

    private void renderPlan(MatrixStack m) {
        int x = cx();
        int y = cy();
        UiModule ui = Plus.ui();
        textRenderer.drawWithShadow(m, "Beat the game roadmap", x, y, TEXT);
        y += 12;
        int line = 10;
        if (ui != null) {
            for (PlanTracker.MilestoneStatus st : ui.plan().statuses()) {
                String mark = switch (st.status()) {
                    case DONE -> "§a✔ ";
                    case CURRENT -> "§e▶ ";
                    case TODO -> "§7○ ";
                };
                textRenderer.drawWithShadow(m, mark + st.milestone().label, x, y, TEXT);
                y += line;
                if (y > bottomY() - 10) break;
            }
        }
        int tx = x + Math.max(170, cw() / 2);
        int ty = cy();
        textRenderer.drawWithShadow(m, "Steps so far", tx, ty, TEXT);
        ty += 12;
        if (ui != null) {
            List<StepTrail.Step> steps = ui.trail().entries();
            int maxLines = (bottomY() - 10 - ty) / line;
            int from = Math.max(0, steps.size() - maxLines);
            for (int i = steps.size() - 1; i >= from; i--) {
                StepTrail.Step s = steps.get(i);
                String time = s.done() ? " (" + (s.durationMs() / 1000) + "s)" : " §e(now)";
                String label = Hud.shorten(s.label(), Math.max(10, (x + cw() - tx) / 6 - 8));
                textRenderer.drawWithShadow(m, label + time, tx, ty, s.done() ? MUTED : TEXT);
                ty += line;
            }
            if (steps.isEmpty()) textRenderer.drawWithShadow(m, "Nothing yet.", tx, ty, MUTED);
        }
    }

    private void renderStatus(MatrixStack m) {
        int x = cx();
        int y = cy();
        Snapshot s = Plus.snapshot();
        int line = 10;
        textRenderer.drawWithShadow(m, "Now", x, y, TEXT);
        y += 12;
        if (s.taskChain.isEmpty()) {
            textRenderer.drawWithShadow(m, "Idle", x, y, MUTED);
            y += line;
        }
        for (int i = 0; i < s.taskChain.size() && i < 6; i++) {
            textRenderer.drawWithShadow(m, "  ".repeat(i) + Hud.shorten(s.taskChain.get(i), 60), x, y, MUTED);
            y += line;
        }
        y += 4;
        textRenderer.drawWithShadow(m, "Inventory: " + s.freeSlots + " free slots, health " + Math.round(s.health) + ", food " + s.food, x, y, MUTED);
        y += 14;

        textRenderer.drawWithShadow(m, "Modules", x, y, TEXT);
        y += 12;
        if (Plus.modules() != null) {
            StringBuilder sb = new StringBuilder();
            for (String id : Plus.modules().ids()) {
                String fail = Plus.modules().failure(id);
                String st = fail != null ? "§cerror" : Plus.modules().isEnabled(id) ? "§aon" : "§7off";
                sb.append(id).append(' ').append(st).append("§r   ");
            }
            textRenderer.drawWithShadow(m, sb.toString(), x, y, MUTED);
            y += line;
        }
        Map<String, Boolean> report = Hooks.report();
        long seen = report.values().stream().filter(Boolean::booleanValue).count();
        textRenderer.drawWithShadow(m, "Hooks: " + seen + "/" + report.size() + " active §7(grey = not used yet)", x, y, MUTED);
        y += line;
        // Word-wrapped list, one colored name at a time, at most 3 lines.
        int hx = x;
        int hookLines = 0;
        for (Map.Entry<String, Boolean> e : report.entrySet()) {
            int w = textRenderer.getWidth(e.getKey() + " ");
            if (hx + w > x + cw() && hx > x) {
                if (++hookLines >= 3) break;
                hx = x;
                y += line;
            }
            textRenderer.drawWithShadow(m, e.getKey(), hx, y, e.getValue() ? 0x55FF55 : 0x777777);
            hx += w;
        }
        y += line + 4;

        textRenderer.drawWithShadow(m, "Log (scroll)", x, y, TEXT);
        y += 12;
        List<LogBuffer.LogLine> lines = Plus.log().snapshot();
        int maxLines = Math.max(1, (bottomY() - 6 - y) / line);
        int end = Math.max(0, lines.size() - logScroll);
        int start = Math.max(0, end - maxLines);
        List<String> shown = new ArrayList<>();
        for (int i = start; i < end; i++) {
            LogBuffer.LogLine l = lines.get(i);
            shown.add("[" + l.source() + "] " + l.message());
        }
        for (String text : shown) {
            textRenderer.drawWithShadow(m, Hud.shorten(text, Math.max(20, cw() / 6)), x, y, MUTED);
            y += line;
        }
    }

    private void renderSettings(MatrixStack m) {
        List<SettingsModel.Row> rows = settingsRows();
        int perPage = rowsPerPage();
        int pages = Math.max(1, (rows.size() + perPage - 1) / perPage);
        int x0 = cx();
        int start = settingsPage * perPage;
        for (int i = start; i < Math.min(rows.size(), start + perPage); i++) {
            SettingsModel.Row row = rows.get(i);
            int[] cell = settingsCell(i - start);
            int x = cell[0];
            int right = cell[1];
            int y = cell[2];
            int labelMax = right - x - (row instanceof SettingsModel.Choice ? 140 : row instanceof SettingsModel.Value ? 100 : 60);
            if (row instanceof SettingsModel.Header h) {
                textRenderer.drawWithShadow(m, "§l" + h.label(), x, y + 6, 0xFFFF55);
            } else if (row instanceof SettingsModel.Toggle t) {
                textRenderer.drawWithShadow(m, fit(t.label(), labelMax), x + 6, y + 6, TEXT);
            } else if (row instanceof SettingsModel.Choice ch) {
                textRenderer.drawWithShadow(m, fit(ch.label(), labelMax), x + 6, y + 6, TEXT);
            } else if (row instanceof SettingsModel.Value v) {
                textRenderer.drawWithShadow(m, fit(v.label(), labelMax), x + 6, y + 6, TEXT);
                String val = String.valueOf(v.get().getAsInt());
                drawCenteredText(m, textRenderer, val, right - 45, y + 6, 0x55FF55);
            }
        }
        drawCenteredText(m, textRenderer, (settingsPage + 1) + " / " + pages, x0 + 50, bottomY() - 20, MUTED);
    }

    // ---- input ----
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tab == Tab.TASKS && grid != null && grid.mouseClicked(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (tab == Tab.TASKS && grid != null && grid.mouseScrolled(mouseX, mouseY, amount)) return true;
        if (tab == Tab.STATUS) {
            logScroll = Math.max(0, Math.min(Plus.log().snapshot().size() - 1, logScroll + (int) Math.signum(amount) * 3));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean typing = (search != null && search.isFocused()) || (command != null && command.isFocused())
                || (count != null && count.isFocused());
        if (!typing && Keybinds.PANEL.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        if (command != null && command.isFocused() && keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
            String c = command.getText().trim();
            if (!c.isEmpty()) run(c.startsWith("@") ? c.substring(1) : c);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if (search != null) search.tick();
        if (count != null) count.tick();
        if (command != null) command.tick();
        if (tab == Tab.ZONES && zonesTab != null) zonesTab.tick();
        if (pauseButton != null) pauseButton.setMessage(pauseLabel());
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
