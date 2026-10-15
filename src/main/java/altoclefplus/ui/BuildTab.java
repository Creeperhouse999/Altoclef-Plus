package altoclefplus.ui;

import altoclefplus.Plus;
import altoclefplus.build.Builds;
import altoclefplus.build.SchematicLibrary;
import altoclefplus.build.logic.BuildJob;
import altoclefplus.build.logic.Materials;
import altoclefplus.build.logic.Placement;
import altoclefplus.build.logic.Schematic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Build tab: pick a .litematic (built-in or from your schematics folder), place it where you stand facing
 * your direction (adjust X/Y/Z, rotate, Relocate), preview the outline, and build. Below: your builds with
 * Resume / Pause / Approve / Stop (Stop asks for a second click because it forgets the build).
 */
final class BuildTab {
    private static String selectedId = "";
    private static boolean fillContents = true;
    private static long confirmStopId = -1;
    private static int fileScroll;

    private final Consumer<ClickableWidget> add;
    private final Runnable rebuild;
    private String loadError = "";
    private static Materials materials;

    BuildTab(Consumer<ClickableWidget> add, Runnable rebuild) {
        this.add = add;
        this.rebuild = rebuild;
    }

    void init(TextRenderer tr, int x, int y, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean inWorld = client.player != null;

        // Files (left column)
        List<SchematicLibrary.Entry> files = SchematicLibrary.list();
        int listW = 150;
        int rows = Math.max(3, Math.min(8, (h - 150) / 22));
        fileScroll = Math.max(0, Math.min(fileScroll, Math.max(0, files.size() - rows)));
        for (int i = 0; i < rows && fileScroll + i < files.size(); i++) {
            SchematicLibrary.Entry e = files.get(fileScroll + i);
            String label = (e.id().startsWith("builtin:") ? "★ " : "") + shorten(e.name(), 18);
            ButtonWidget b = new ButtonWidget(x, y + i * 22, listW, 20, new LiteralText(label), btn -> select(e.id()));
            b.active = !e.id().equals(selectedId);
            add.accept(b);
        }
        int below = y + rows * 22;
        add.accept(new ButtonWidget(x, below, 22, 20, new LiteralText("▲"), b -> { fileScroll--; rebuild.run(); }));
        add.accept(new ButtonWidget(x + 24, below, 22, 20, new LiteralText("▼"), b -> { fileScroll++; rebuild.run(); }));
        add.accept(new ButtonWidget(x + 48, below, 102, 20, new LiteralText("Open folder"), b -> openFolder()));

        // Placement + build (right column)
        int rx = x + listW + 10;
        int ry = y + 44;
        if (Builds.previewSchematic != null && Builds.previewPlacement != null) {
            add.accept(small(rx, ry, "X-", () -> move(-1, 0, 0)));
            add.accept(small(rx + 24, ry, "X+", () -> move(1, 0, 0)));
            add.accept(small(rx + 52, ry, "Y-", () -> move(0, -1, 0)));
            add.accept(small(rx + 76, ry, "Y+", () -> move(0, 1, 0)));
            add.accept(small(rx + 104, ry, "Z-", () -> move(0, 0, -1)));
            add.accept(small(rx + 128, ry, "Z+", () -> move(0, 0, 1)));
            add.accept(new ButtonWidget(rx + 156, ry, 50, 20, new LiteralText("Rotate"), b -> {
                Builds.previewPlacement = Builds.previewPlacement.rotated(1);
            }));
            ButtonWidget relocate = new ButtonWidget(rx + 210, ry, 64, 20, new LiteralText("Relocate"), b -> relocate());
            relocate.active = inWorld;
            add.accept(relocate);
            int ry2 = ry + 24;
            add.accept(new ButtonWidget(rx, ry2, 100, 20, new LiteralText("Preview: " + (Builds.previewOn ? "§aON" : "§cOFF")), b -> {
                Builds.previewOn = !Builds.previewOn;
                rebuild.run();
            }));
            add.accept(new ButtonWidget(rx + 104, ry2, 170, 20, new LiteralText("Fill chests/signs/frames: " + (fillContents ? "§aON" : "§cOFF")), b -> {
                fillContents = !fillContents;
                rebuild.run();
            }));
            ButtonWidget build = new ButtonWidget(rx, ry2 + 24, 274, 20, new LiteralText("Build it"), b -> startBuild());
            build.active = inWorld;
            add.accept(build);
        }

        // Jobs (bottom)
        int jy = y + h - 22 * Math.min(4, Builds.queue().forWorld(Builds.world()).size()) - 4;
        List<BuildJob> jobs = new ArrayList<>(Builds.queue().forWorld(Builds.world()));
        for (int i = 0; i < jobs.size() && i < 4; i++) {
            BuildJob j = jobs.get(i);
            int bx = x + w - 230;
            int by = jy + i * 22;
            switch (j.state) {
                case PAUSED, QUEUED -> add.accept(new ButtonWidget(bx, by, 70, 20, new LiteralText(j.state == BuildJob.State.PAUSED ? "Resume" : "Queued"), b -> {
                    Builds.resume(j);
                    rebuild.run();
                }));
                case RUNNING -> add.accept(new ButtonWidget(bx, by, 70, 20, new LiteralText("Pause"), b -> {
                    Builds.pause(j);
                    rebuild.run();
                }));
                case WAITING_APPROVAL -> add.accept(new ButtonWidget(bx, by, 70, 20, new LiteralText("§eApprove"), b -> {
                    Builds.approve(j);
                    rebuild.run();
                }));
                case DONE -> add.accept(new ButtonWidget(bx, by, 70, 20, new LiteralText("Repair"), b -> {
                    Builds.resume(repairCopy(j));
                    rebuild.run();
                }));
            }
            boolean confirming = confirmStopId == j.id;
            add.accept(new ButtonWidget(bx + 74, by, 80, 20, new LiteralText(confirming ? "§cSure? Stop" : (j.state == BuildJob.State.DONE ? "Remove" : "Stop")), b -> {
                if (confirmStopId == j.id || j.state == BuildJob.State.DONE) {
                    Builds.stop(j);
                    confirmStopId = -1;
                } else {
                    confirmStopId = j.id;
                }
                rebuild.run();
            }));
        }
    }

    void render(MatrixStack m, TextRenderer tr, int x, int y, int w, int h) {
        int rx = x + 160;
        if (SchematicLibrary.list().isEmpty()) {
            tr.drawWithShadow(m, "No .litematic files yet. Put some in:", rx, y, 0xAAAAAA);
            tr.drawWithShadow(m, SchematicLibrary.folder().toString(), rx, y + 10, 0x777777);
        }
        Schematic s = Builds.previewSchematic;
        if (s != null && Builds.previewPlacement != null) {
            tr.drawWithShadow(m, "§l" + shorten(s.name().isEmpty() ? selectedId : s.name(), 30), rx, y, 0xFFFFFF);
            tr.drawWithShadow(m, s.sizeX() + " x " + s.sizeY() + " x " + s.sizeZ() + ", " + s.nonAirCount() + " blocks"
                    + (s.author().isEmpty() ? "" : " - by " + shorten(s.author(), 16)), rx, y + 11, 0xAAAAAA);
            Placement p = Builds.previewPlacement;
            tr.drawWithShadow(m, "At " + p.originX() + ", " + p.originY() + ", " + p.originZ() + "  facing "
                    + facingName(p.rotation()), rx, y + 22, 0xAAAAAA);
            if (materials != null) {
                int my = y + 44 + 24 * 3 + 4;
                if (!materials.unplaceable().isEmpty()) {
                    tr.drawWithShadow(m, "§eCan't be placed in survival: " + shorten(describe(materials.unplaceable()), 50), rx, my, 0xFFFF55);
                    my += 10;
                }
                tr.drawWithShadow(m, "Needs (have / need):", rx, my, 0xFFFFFF);
                my += 10;
                int shown = 0;
                for (Map.Entry<String, Integer> e : materials.items().entrySet()) {
                    if (shown++ >= 5) break;
                    int have = haveCount(e.getKey());
                    int color = have >= e.getValue() ? 0x55FF55 : 0xAAAAAA;
                    tr.drawWithShadow(m, "  " + e.getKey().replace("minecraft:", "") + "  " + have + " / " + e.getValue(), rx, my, color);
                    my += 10;
                }
                if (materials.items().size() > 5) tr.drawWithShadow(m, "  ... " + (materials.items().size() - 5) + " more", rx, my, 0x777777);
            }
        } else if (!loadError.isEmpty()) {
            tr.drawWithShadow(m, "§c" + shorten(loadError, 60), rx, y, 0xFF5555);
        } else if (!SchematicLibrary.list().isEmpty()) {
            tr.drawWithShadow(m, "Pick a schematic on the left.", rx, y, 0xAAAAAA);
        }

        // Jobs
        List<BuildJob> jobs = Builds.queue().forWorld(Builds.world());
        int jy = y + h - 22 * Math.min(4, jobs.size()) - 4;
        if (!jobs.isEmpty()) {
            tr.drawWithShadow(m, "Your builds (a build may stop and wait for your approval):", x, jy - 12, 0xFFFF55);
        } else {
            tr.drawWithShadow(m, "A build may stop and wait for your approval (blocks it can't place or get).", x, y + h - 10, 0x777777);
        }
        for (int i = 0; i < jobs.size() && i < 4; i++) {
            BuildJob j = jobs.get(i);
            String progress = j.total > 0 ? " " + j.placed + "/" + j.total : "";
            tr.drawWithShadow(m, shorten(j.name, 16) + " - " + stateName(j.state) + progress, x, jy + i * 22 + 6, 0xFFFFFF);
            if (!j.note.isEmpty()) tr.drawWithShadow(m, shorten(j.note, 40), x + 150, jy + i * 22 + 6, 0xFFAA00);
        }
    }

    private void select(String id) {
        selectedId = id;
        loadError = "";
        try {
            Builds.previewSchematic = SchematicLibrary.load(id);
            materials = Materials.of(Builds.previewSchematic);
            if (Builds.previewPlacement == null) relocate();
        } catch (Exception e) {
            Builds.previewSchematic = null;
            materials = null;
            loadError = e.getMessage();
        }
        rebuild.run();
    }

    /** Back to you: the block in front of your feet, facing the way you look. */
    private void relocate() {
        var p = MinecraftClient.getInstance().player;
        if (p == null) return;
        Direction facing = p.getHorizontalFacing();
        BlockPos o = p.getBlockPos().offset(facing);
        Builds.previewPlacement = new Placement(o.getX(), o.getY(), o.getZ(), Placement.rotationForFacing(facing.asString()));
    }

    private void move(int dx, int dy, int dz) {
        Builds.previewPlacement = Builds.previewPlacement.moved(dx, dy, dz);
    }

    private void startBuild() {
        Schematic s = Builds.previewSchematic;
        if (s == null || Builds.previewPlacement == null) return;
        String name = s.name().isEmpty() ? selectedId.substring(selectedId.indexOf(':') + 1).replace(".litematic", "") : s.name();
        Builds.add(BuildJob.create(Builds.world(), selectedId, name, Builds.previewPlacement, fillContents));
        rebuild.run();
    }

    /** Repair: the same build again; it only places what is missing or broken now. */
    private static BuildJob repairCopy(BuildJob done) {
        Builds.queue().stop(done);
        BuildJob j = BuildJob.create(done.world, done.file, done.name, done.placement(), done.fillContents);
        j.approvedSkipImpossible = done.approvedSkipImpossible;
        j.skipBlocks = new ArrayList<>(done.skipBlocks);
        Builds.queue().add(j);
        j.state = BuildJob.State.PAUSED;
        return j;
    }

    private static void openFolder() {
        try {
            java.nio.file.Files.createDirectories(SchematicLibrary.folder());
            Util.getOperatingSystem().open(SchematicLibrary.folder().toFile());
        } catch (Exception ignored) {
        }
        SchematicLibrary.clearCache();
    }

    private static int haveCount(String itemId) {
        Integer n = Plus.snapshot().items.get(itemId.replace("minecraft:", ""));
        return n == null ? 0 : n;
    }

    private static ButtonWidget small(int x, int y, String label, Runnable r) {
        return new ButtonWidget(x, y, 22, 20, new LiteralText(label), b -> r.run());
    }

    private static String describe(Map<String, Integer> blocks) {
        StringBuilder sb = new StringBuilder();
        blocks.forEach((b, n) -> sb.append(sb.length() > 0 ? ", " : "").append(b.replace("minecraft:", "")).append(" x").append(n));
        return sb.toString();
    }

    private static String facingName(int rotation) {
        return switch (rotation) {
            case 1 -> "west";
            case 2 -> "north";
            case 3 -> "east";
            default -> "south";
        };
    }

    private static String stateName(BuildJob.State s) {
        return switch (s) {
            case QUEUED -> "queued";
            case RUNNING -> "§abuilding§r";
            case PAUSED -> "§epaused§r";
            case WAITING_APPROVAL -> "§6waiting for you§r";
            case DONE -> "§7done§r";
        };
    }

    private static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
