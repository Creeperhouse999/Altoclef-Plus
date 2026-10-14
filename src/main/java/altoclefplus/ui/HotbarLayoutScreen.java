package altoclefplus.ui;

import altoclefplus.Plus;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.List;
import java.util.Map;

/** Chooses which hotbar slot (1-9, or none) each item category lives in. */
public final class HotbarLayoutScreen extends PlusScreen {
    private static final List<String> CATEGORIES = List.of("sword", "pickaxe", "axe", "shovel", "blocks", "food", "water_bucket");

    private final Screen parent;

    public HotbarLayoutScreen(Screen parent) {
        super(new LiteralText("Hotbar layout"));
        this.parent = parent;
    }

    private Map<String, Integer> layout() {
        return Plus.config().inventory.hotbarLayout;
    }

    @Override
    protected void init() {
        int y = 36;
        int x = width / 2 + 20;
        for (String cat : CATEGORIES) {
            addDrawableChild(new ButtonWidget(x, y, 20, 20, new LiteralText("-"), b -> shift(cat, -1)));
            addDrawableChild(new ButtonWidget(x + 60, y, 20, 20, new LiteralText("+"), b -> shift(cat, +1)));
            y += 24;
        }
        addDrawableChild(new ButtonWidget(width / 2 - 30, height - 28, 60, 20, new LiteralText("Done"), b -> close()));
    }

    /** Cycles none(-1) .. 8; a slot taken by another category is swapped with it. */
    private void shift(String cat, int dir) {
        int cur = layout().getOrDefault(cat, -1);
        int next = cur + dir;
        if (next < -1) next = 8;
        if (next > 8) next = -1;
        if (next >= 0) {
            for (Map.Entry<String, Integer> e : layout().entrySet()) {
                if (!e.getKey().equals(cat) && e.getValue() == next) e.setValue(cur);
            }
        }
        layout().put(cat, next);
    }

    @Override
    public void render(MatrixStack m, int mouseX, int mouseY, float delta) {
        renderBackground(m);
        drawCenteredText(m, textRenderer, title, width / 2, 12, 0xFFFFFF);
        int y = 42;
        for (String cat : CATEGORIES) {
            textRenderer.drawWithShadow(m, cat.replace('_', ' '), width / 2 - 110, y, 0xFFFFFF);
            int slot = layout().getOrDefault(cat, -1);
            drawCenteredText(m, textRenderer, slot < 0 ? "none" : "slot " + (slot + 1), width / 2 + 50, y, 0x55FF55);
            y += 24;
        }
        super.render(m, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        Plus.saveConfig();
        client.setScreen(parent);
    }
}
