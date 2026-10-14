package altoclefplus.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Scrollable, searchable grid of item icons. Not a widget: the owning screen forwards input. */
public final class ItemGrid {
    private static final int CELL = 18;

    private final Consumer<String> onClick;
    private final Predicate<String> highlighted;
    private List<Item> items = new ArrayList<>();
    private int x, y, w, h;
    private int scrollRow;

    public ItemGrid(Consumer<String> onClick, Predicate<String> highlighted) {
        this.onClick = onClick;
        this.highlighted = highlighted;
        setFilter("");
    }

    public void setBounds(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void setFilter(String text) {
        String q = text.trim().toLowerCase(Locale.ROOT);
        List<Item> out = new ArrayList<>();
        for (Item item : Registry.ITEM) {
            if (item == Items.AIR) continue;
            String id = Registry.ITEM.getId(item).getPath();
            if (q.isEmpty() || id.contains(q.replace(' ', '_'))
                    || item.getName().getString().toLowerCase(Locale.ROOT).contains(q)) {
                out.add(item);
            }
        }
        items = out;
        scrollRow = 0;
    }

    private int cols() {
        return Math.max(1, w / CELL);
    }

    private int visibleRows() {
        return Math.max(1, h / CELL);
    }

    public void render(MatrixStack m, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        DrawableHelper.fill(m, x - 1, y - 1, x + cols() * CELL + 1, y + visibleRows() * CELL + 1, 0x80000000);
        Item hovered = null;
        int start = scrollRow * cols();
        int end = Math.min(items.size(), start + cols() * visibleRows());
        for (int i = start; i < end; i++) {
            int col = (i - start) % cols();
            int row = (i - start) / cols();
            int cx = x + col * CELL;
            int cy = y + row * CELL;
            Item item = items.get(i);
            String id = Registry.ITEM.getId(item).getPath();
            if (highlighted.test(id)) DrawableHelper.fill(m, cx, cy, cx + CELL, cy + CELL, 0x8055FF55);
            if (mouseX >= cx && mouseX < cx + CELL && mouseY >= cy && mouseY < cy + CELL) {
                DrawableHelper.fill(m, cx, cy, cx + CELL, cy + CELL, 0x60FFFFFF);
                hovered = item;
            }
            client.getItemRenderer().renderInGuiWithOverrides(new ItemStack(item), cx + 1, cy + 1);
        }
        if (hovered != null && client.currentScreen != null) {
            client.currentScreen.renderTooltip(m, hovered.getName(), mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mx, double my) {
        if (mx < x || my < y || mx >= x + cols() * CELL || my >= y + visibleRows() * CELL) return false;
        int idx = scrollRow * cols() + ((int) (my - y) / CELL) * cols() + (int) (mx - x) / CELL;
        if (idx >= 0 && idx < items.size()) {
            onClick.accept(Registry.ITEM.getId(items.get(idx)).getPath());
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double amount) {
        if (mx < x || my < y || mx >= x + w || my >= y + h) return false;
        int maxRow = Math.max(0, (items.size() + cols() - 1) / cols() - visibleRows());
        scrollRow = Math.max(0, Math.min(maxRow, scrollRow - (int) Math.signum(amount)));
        return true;
    }
}
