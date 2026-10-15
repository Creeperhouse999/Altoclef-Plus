package altoclefplus.ui;

import altoclefplus.Plus;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Edits a list of mob types (e.g. "never attack"): search, then click names to add/remove them. */
public final class EntityListScreen extends PlusScreen {
    private static final int ROW_H = 22;
    private final Screen parent;
    private final List<String> list;
    private TextFieldWidget search;
    private String filter = "";
    private int page;

    public EntityListScreen(Screen parent, String title, List<String> list) {
        super(new LiteralText(title));
        this.parent = parent;
        this.list = list;
    }

    private List<String> mobIds() {
        List<String> ids = new ArrayList<>();
        for (EntityType<?> type : Registry.ENTITY_TYPE) {
            if (type.getSpawnGroup() == SpawnGroup.MISC) continue; // items, arrows, boats...
            String id = Registry.ENTITY_TYPE.getId(type).getPath();
            String name = type.getName().getString().toLowerCase(Locale.ROOT);
            if (filter.isEmpty() || id.contains(filter) || name.contains(filter)) ids.add(id);
        }
        ids.sort(String::compareTo);
        return ids;
    }

    @Override
    protected void init() {
        search = addDrawableChild(new TextFieldWidget(textRenderer, 60, 28, 160, 18, new LiteralText("Search")));
        search.setText(filter);
        search.setChangedListener(t -> {
            filter = t.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
            page = 0;
            clearChildren();
            init();
            search.setTextFieldFocused(true);
        });
        List<String> ids = mobIds();
        int cols = Math.max(1, (width - 20) / 140);
        int rows = Math.max(1, (height - 52 - 34) / ROW_H);
        int perPage = cols * rows;
        int pages = Math.max(1, (ids.size() + perPage - 1) / perPage);
        page = Math.min(page, pages - 1);
        for (int i = page * perPage; i < Math.min(ids.size(), (page + 1) * perPage); i++) {
            String id = ids.get(i);
            int n = i - page * perPage;
            int x = 10 + (n % cols) * 140;
            int y = 52 + (n / cols) * ROW_H;
            addDrawableChild(new ButtonWidget(x, y, 134, 20, label(id), b -> {
                if (!list.remove(id)) list.add(id);
                b.setMessage(label(id));
            }));
        }
        ButtonWidget prev = addDrawableChild(new ButtonWidget(10, height - 26, 20, 20, new LiteralText("<"), b -> {
            page--;
            clearChildren();
            init();
        }));
        prev.active = page > 0;
        ButtonWidget next = addDrawableChild(new ButtonWidget(34, height - 26, 20, 20, new LiteralText(">"), b -> {
            page++;
            clearChildren();
            init();
        }));
        next.active = page < pages - 1;
        addDrawableChild(new ButtonWidget(width - 70, height - 26, 60, 20, new LiteralText("Done"), b -> close()));
    }

    private LiteralText label(String id) {
        return new LiteralText((list.contains(id) ? "§a✔ " : "§7") + id.replace('_', ' '));
    }

    @Override
    public void render(MatrixStack m, int mouseX, int mouseY, float delta) {
        renderBackground(m);
        drawCenteredText(m, textRenderer, title, width / 2, 10, 0xFFFFFF);
        textRenderer.drawWithShadow(m, "Search:", 10, 33, 0xFFFFFF);
        textRenderer.drawWithShadow(m, "Green = in the list. " + list.size() + " selected.", 60, height - 20, 0xAAAAAA);
        super.render(m, mouseX, mouseY, delta);
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
