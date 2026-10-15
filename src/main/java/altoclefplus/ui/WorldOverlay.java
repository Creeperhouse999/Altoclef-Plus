package altoclefplus.ui;

import altoclefplus.Plus;
import altoclefplus.core.logic.PlusConfig;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalTwoBlocks;
import baritone.api.pathing.path.IPathExecutor;
import baritone.api.utils.BetterBlockPos;
import net.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Our own world overlay (replaces Baritone's while on): blocks the bot will break (red), the block it is
 * mining (orange), its goal (green), the path ahead (a cyan line) and nearby hostile mobs (red outlines).
 * Each layer can be switched off in Settings.
 */
public final class WorldOverlay {
    private static final Pattern DESTROY = Pattern.compile("Destroy block at (-?\\d+), (-?\\d+), (-?\\d+)");
    private static Boolean appliedHideBaritone;

    private WorldOverlay() {}

    public static void render(MatrixStack matrices, Camera camera, float tickDelta) {
        if (Plus.mod() == null) return;
        PlusConfig.Ui cfg = Plus.config().ui;
        syncBaritoneRendering(cfg.overlay);
        MinecraftClient client = MinecraftClient.getInstance();
        if (!cfg.overlay || client.world == null || client.player == null) return;
        Vec3d cam = camera.getPos();
        var pathing = Plus.mod().getClientBaritone().getPathingBehavior();
        IPathExecutor exec = pathing.getCurrent();
        BlockPos mining = miningTarget();
        BlockPos goal = goalPos(pathing.getGoal());
        Collection<BlockPos> toBreak = cfg.overlayBreak && exec instanceof baritone.pathing.path.PathExecutor pe ? pe.toBreak() : List.of();

        boolean fillTarget = cfg.overlayTarget && mining != null;
        boolean fillGoal = cfg.overlayGoal && goal != null;
        List<HostileEntity> mobs = cfg.overlayMobs ? client.world.getEntitiesByClass(HostileEntity.class,
                client.player.getBoundingBox().expand(16), Entity::isAlive) : List.of();
        boolean path = cfg.overlayPath && exec != null;
        Box preview = buildPreviewBox(cam);
        Box running = runningBuildBox(cam);
        if (toBreak.isEmpty() && !fillTarget && !fillGoal && mobs.isEmpty() && !path && preview == null && running == null) return;

        // Drawn straight to the screen like Baritone does (not through render layers), so it also shows with
        // "Fabulous" graphics, where those layers go to buffers that are already combined at this point.
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        Matrix4f pos = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        try {
            // Soft translucent fills first...
            if (!toBreak.isEmpty() || fillTarget || fillGoal) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                for (BlockPos b : toBreak) fillBox(buf, pos, blockBox(b, cam, 0.002), 1f, 0.25f, 0.25f, 0.18f);
                if (fillTarget) fillBox(buf, pos, blockBox(mining, cam, 0.004), 1f, 0.6f, 0.1f, 0.30f);
                if (fillGoal) fillBox(buf, pos, blockBox(goal, cam, 0.004), 0.3f, 1f, 0.4f, 0.22f);
                tess.draw();
            }
            // ...then crisp outlines and the path line.
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
            RenderSystem.lineWidth(2f);
            buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            for (BlockPos b : toBreak) WorldRenderer.drawBox(matrices, buf, blockBox(b, cam, 0.002), 1f, 0.3f, 0.3f, 0.9f);
            if (fillTarget) WorldRenderer.drawBox(matrices, buf, blockBox(mining, cam, 0.004), 1f, 0.65f, 0.15f, 1f);
            if (fillGoal) WorldRenderer.drawBox(matrices, buf, blockBox(goal, cam, 0.004), 0.35f, 1f, 0.45f, 1f);
            for (HostileEntity e : mobs) {
                double dx = e.prevX + (e.getX() - e.prevX) * tickDelta - e.getX();
                double dy = e.prevY + (e.getY() - e.prevY) * tickDelta - e.getY();
                double dz = e.prevZ + (e.getZ() - e.prevZ) * tickDelta - e.getZ();
                Box box = e.getBoundingBox().offset(dx - cam.x, dy - cam.y, dz - cam.z);
                WorldRenderer.drawBox(matrices, buf, box, 1f, 0.25f, 0.25f, 0.9f);
            }
            if (path) drawPath(matrices, buf, exec.getPath(), Math.max(0, exec.getPosition()), cam);
            if (preview != null) WorldRenderer.drawBox(matrices, buf, preview, 0.3f, 0.9f, 1f, 1f);
            if (running != null) WorldRenderer.drawBox(matrices, buf, running, 0.75f, 0.45f, 1f, 1f);
            tess.draw();
        } finally {
            RenderSystem.lineWidth(1f);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    /** Outline of the schematic being placed in the Build tab (cyan), while Preview is on. */
    private static Box buildPreviewBox(Vec3d cam) {
        var s = altoclefplus.build.Builds.previewSchematic;
        var p = altoclefplus.build.Builds.previewPlacement;
        if (!altoclefplus.build.Builds.previewOn || s == null || p == null) return null;
        int[] b = p.bounds(s.sizeX(), s.sizeY(), s.sizeZ());
        return new Box(b[0], b[1], b[2], b[3] + 1, b[4] + 1, b[5] + 1).offset(-cam.x, -cam.y, -cam.z);
    }

    /** Outline of the build in progress (purple). */
    private static Box runningBuildBox(Vec3d cam) {
        var job = altoclefplus.build.Builds.running().orElse(null);
        if (job == null) return null;
        try {
            var s = altoclefplus.build.SchematicLibrary.load(job.file);
            int[] b = job.placement().bounds(s.sizeX(), s.sizeY(), s.sizeZ());
            return new Box(b[0], b[1], b[2], b[3] + 1, b[4] + 1, b[5] + 1).offset(-cam.x, -cam.y, -cam.z);
        } catch (Exception e) {
            return null;
        }
    }

    /** Safe-zone outlines when "Show safe zones" is on (plus the rectangle at your feet for full-height zones). */
    public static void renderZones(MatrixStack matrices, Camera camera) {
        if (Plus.mod() == null || !Plus.config().ui.showZones) return;
        MinecraftClient client = MinecraftClient.getInstance();
        var zones = altoclefplus.zones.Zones.current().zones();
        if (client.world == null || zones.isEmpty()) return;
        Vec3d cam = camera.getPos();
        int bottom = client.world.getBottomY();
        int top = client.world.getTopY();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(2f);
        try {
            buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            for (altoclefplus.zones.logic.Zone z : zones) {
                double minY = z.fullHeight ? Math.max(bottom, cam.y - 48) : z.minY;
                double maxY = z.fullHeight ? Math.min(top, cam.y + 48) : z.maxY + 1;
                Box box = new Box(z.minX, minY, z.minZ, z.maxX + 1, maxY, z.maxZ + 1).offset(-cam.x, -cam.y, -cam.z);
                WorldRenderer.drawBox(matrices, buf, box, 0.35f, 0.9f, 0.45f, 1.0f);
                if (z.fullHeight && client.player != null) {
                    // Full-height zones reach far above and below: also draw the rectangle at your feet.
                    double fy = Math.floor(client.player.getY());
                    Box ground = new Box(z.minX, fy, z.minZ, z.maxX + 1, fy + 0.05, z.maxZ + 1).offset(-cam.x, -cam.y, -cam.z);
                    WorldRenderer.drawBox(matrices, buf, ground, 0.45f, 1.0f, 0.55f, 1.0f);
                }
            }
            tess.draw();
        } finally {
            RenderSystem.lineWidth(1f);
            RenderSystem.disableBlend();
        }
    }

    private static void drawPath(MatrixStack matrices, VertexConsumer lines, IPath path, int from, Vec3d cam) {
        List<BetterBlockPos> pts = path.positions();
        Matrix4f pos = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();
        for (int i = from; i + 1 < pts.size(); i++) {
            BlockPos a = pts.get(i);
            BlockPos b = pts.get(i + 1);
            float ax = (float) (a.getX() + 0.5 - cam.x), ay = (float) (a.getY() + 0.1 - cam.y), az = (float) (a.getZ() + 0.5 - cam.z);
            float bx = (float) (b.getX() + 0.5 - cam.x), by = (float) (b.getY() + 0.1 - cam.y), bz = (float) (b.getZ() + 0.5 - cam.z);
            float nx = bx - ax, ny = by - ay, nz = bz - az;
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 1e-4) continue;
            nx /= len;
            ny /= len;
            nz /= len;
            lines.vertex(pos, ax, ay, az).color(0.2f, 0.85f, 1f, 1f).normal(normal, nx, ny, nz).next();
            lines.vertex(pos, bx, by, bz).color(0.2f, 0.85f, 1f, 1f).normal(normal, nx, ny, nz).next();
        }
    }

    private static Box blockBox(BlockPos b, Vec3d cam, double grow) {
        return new Box(b).expand(grow).offset(-cam.x, -cam.y, -cam.z);
    }

    private static void fillBox(VertexConsumer q, Matrix4f m, Box b, float r, float g, float bl, float a) {
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;
        quad(q, m, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, bl, a); // bottom
        quad(q, m, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, r, g, bl, a); // top
        quad(q, m, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, r, g, bl, a); // north
        quad(q, m, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r, g, bl, a); // south
        quad(q, m, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, bl, a); // west
        quad(q, m, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, r, g, bl, a); // east
    }

    private static void quad(VertexConsumer q, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz, float r, float g, float b, float a) {
        q.vertex(m, ax, ay, az).color(r, g, b, a).next();
        q.vertex(m, bx, by, bz).color(r, g, b, a).next();
        q.vertex(m, cx, cy, cz).color(r, g, b, a).next();
        q.vertex(m, dx, dy, dz).color(r, g, b, a).next();
    }

    /** The block in AltoClef's "Destroy block at x, y, z" step, if any. */
    private static BlockPos miningTarget() {
        List<String> chain = Plus.snapshot().taskChain;
        for (int i = chain.size() - 1; i >= 0; i--) {
            Matcher mm = DESTROY.matcher(chain.get(i));
            if (mm.find()) return new BlockPos(Integer.parseInt(mm.group(1)), Integer.parseInt(mm.group(2)), Integer.parseInt(mm.group(3)));
        }
        return null;
    }

    private static BlockPos goalPos(Goal g) {
        if (g instanceof GoalBlock b) return b.getGoalPos();
        if (g instanceof GoalGetToBlock b) return b.getGoalPos();
        if (g instanceof GoalTwoBlocks b) return b.getGoalPos();
        if (g instanceof GoalNear b) return b.getGoalPos();
        return null;
    }

    /** While our overlay is on, Baritone's own path/goal/box drawing is off (and back on when ours is off). */
    private static void syncBaritoneRendering(boolean ours) {
        if (appliedHideBaritone != null && appliedHideBaritone == ours) return;
        var bs = Plus.mod().getClientBaritoneSettings();
        bs.renderPath.value = !ours;
        bs.renderGoal.value = !ours;
        bs.renderSelectionBoxes.value = !ours;
        appliedHideBaritone = ours;
    }
}
