package altoclefplus.ui;

import altoclefplus.Plus;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.List;

/** Edits an item-id list (protected items, junk items): click an icon to add/remove it. */
public final class ItemListScreen extends PlusScreen {
    private final Screen parent;
    private final List<String> list;
    private ItemGrid grid;
    private TextFieldWidget search;

    public ItemListScreen(Screen parent, String title, List<String> list) {
        super(new LiteralText(title));
        this.parent = parent;
        this.list = list;
    }

    @Override
    protected void init() {
        search = addDrawableChild(new TextFieldWidget(textRenderer, 60, 28, 160, 18, new LiteralText("Search")));
        search.setChangedListener(t -> grid.setFilter(t));
        grid = new ItemGrid(id -> {
            if (!list.remove(id)) list.add(id);
        }, list::contains);
        grid.setBounds(10, 52, width - 20, height - 52 - 34);
        addDrawableChild(new ButtonWidget(width - 70, height - 26, 60, 20, new LiteralText("Done"), b -> close()));
    }

    @Override
    public void render(MatrixStack m, int mouseX, int mouseY, float delta) {
        renderBackground(m);
        drawCenteredText(m, textRenderer, title, width / 2, 10, 0xFFFFFF);
        textRenderer.drawWithShadow(m, "Search:", 10, 33, 0xFFFFFF);
        textRenderer.drawWithShadow(m, "Green = in the list. Click to add or remove. " + list.size() + " items.", 10, height - 20, 0xAAAAAA);
        super.render(m, mouseX, mouseY, delta);
        grid.render(m, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (grid.mouseClicked(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (grid.mouseScrolled(mouseX, mouseY, amount)) return true;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void tick() {
        search.tick();
    }

    @Override
    public void close() {
        Plus.saveConfig();
        client.setScreen(parent);
    }
}
