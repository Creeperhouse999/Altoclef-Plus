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
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
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

        VertexConsumerProvider.Immediate buffers = client.getBufferBuilders().getEntityVertexConsumers();

        // Soft glowing fills first (lightning layer: position+color quads, additive blend)...
        VertexConsumer fill = buffers.getBuffer(RenderLayer.getLightning());
        Matrix4f pos = matrices.peek().getPositionMatrix();
        for (BlockPos b : toBreak) fillBox(fill, pos, blockBox(b, cam, 0.002), 1f, 0.25f, 0.25f, 0.18f);
        if (cfg.overlayTarget && mining != null) fillBox(fill, pos, blockBox(mining, cam, 0.004), 1f, 0.6f, 0.1f, 0.30f);
        if (cfg.overlayGoal && goal != null) fillBox(fill, pos, blockBox(goal, cam, 0.004), 0.3f, 1f, 0.4f, 0.22f);
        buffers.draw(RenderLayer.getLightning());

        // ...then crisp outlines and the path line.
        VertexConsumer lines = buffers.getBuffer(RenderLayer.getLines());
        for (BlockPos b : toBreak) WorldRenderer.drawBox(matrices, lines, blockBox(b, cam, 0.002), 1f, 0.3f, 0.3f, 0.9f);
        if (cfg.overlayTarget && mining != null) WorldRenderer.drawBox(matrices, lines, blockBox(mining, cam, 0.004), 1f, 0.65f, 0.15f, 1f);
        if (cfg.overlayGoal && goal != null) WorldRenderer.drawBox(matrices, lines, blockBox(goal, cam, 0.004), 0.35f, 1f, 0.45f, 1f);
        if (cfg.overlayMobs) {
            for (HostileEntity e : client.world.getEntitiesByClass(HostileEntity.class,
                    client.player.getBoundingBox().expand(16), Entity::isAlive)) {
                double dx = e.prevX + (e.getX() - e.prevX) * tickDelta - e.getX();
                double dy = e.prevY + (e.getY() - e.prevY) * tickDelta - e.getY();
                double dz = e.prevZ + (e.getZ() - e.prevZ) * tickDelta - e.getZ();
                Box box = e.getBoundingBox().offset(dx - cam.x, dy - cam.y, dz - cam.z);
                WorldRenderer.drawBox(matrices, lines, box, 1f, 0.25f, 0.25f, 0.9f);
            }
        }
        if (cfg.overlayPath && exec != null) drawPath(matrices, lines, exec.getPath(), Math.max(0, exec.getPosition()), cam);
        buffers.draw(RenderLayer.getLines());
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
