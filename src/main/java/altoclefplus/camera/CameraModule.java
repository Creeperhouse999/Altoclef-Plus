package altoclefplus.camera;

import altoclefplus.Plus;
import altoclefplus.camera.logic.CameraPolicy;
import altoclefplus.camera.logic.CameraSmoother;
import altoclefplus.core.Hooks;
import altoclefplus.core.logic.Module;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Visual-only camera smoothing. The render camera gets smoothed angles; the player's real rotation (used by
 * the bot for mining, placing and fighting, and sent to the server) is never touched.
 */
public final class CameraModule implements Module {
    private static CameraModule instance;

    private final CameraSmoother smoother = new CameraSmoother();
    private volatile boolean botActive;
    private volatile boolean combat;
    private long lastFrameNs;
    private double lastX, lastY, lastZ;
    private String lastDimension = "";

    public CameraModule() {
        instance = this;
    }

    @Override
    public String id() {
        return "camera";
    }

    @Override
    public void tick(Snapshot s) {
        botActive = s.userTaskRunning || s.pathing;
        combat = s.mobDefenseActive;
    }

    @Override
    public String status() {
        return Hooks.seen("camera") ? "ok" : "waiting for first frame";
    }

    /** Called from the camera mixin with the angles vanilla would use. Returns {yaw, pitch} to render. */
    public static float[] apply(float yaw, float pitch) {
        Hooks.hit("camera");
        CameraModule m = instance;
        if (m == null) return new float[]{yaw, pitch};
        return m.applyInternal(yaw, pitch);
    }

    private float[] applyInternal(float yaw, float pitch) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        long now = System.nanoTime();
        float dt = lastFrameNs == 0 ? 0f : Math.min(0.25f, (now - lastFrameNs) / 1_000_000_000f);
        lastFrameNs = now;
        if (player == null || client.world == null) {
            smoother.snap(yaw, pitch);
            return new float[]{yaw, pitch};
        }

        double moved = Math.sqrt(player.squaredDistanceTo(lastX, lastY, lastZ));
        lastX = player.getX();
        lastY = player.getY();
        lastZ = player.getZ();
        String dim = client.world.getRegistryKey().getValue().getPath();
        boolean dimChanged = !dim.equals(lastDimension);
        lastDimension = dim;

        PlusConfig.Camera cfg = Plus.config().camera;
        boolean enabled = Plus.modules() != null && Plus.modules().isEnabled("camera");
        CameraPolicy.Decision d = CameraPolicy.decide(new CameraPolicy.Input(
                enabled, botActive, client.currentScreen != null, moved, dimChanged, combat), cfg.snapDistance);

        if (d.mode() == CameraPolicy.Mode.SNAP) {
            smoother.snap(yaw, pitch);
        } else {
            float k = cfg.smoothness * (d.combat() ? cfg.combatMultiplier : 1f);
            float maxSpeed = cfg.maxDegreesPerSecond * (d.combat() ? cfg.combatMultiplier : 1f);
            smoother.update(yaw, pitch, dt, k, maxSpeed);
        }
        return new float[]{smoother.yaw(), smoother.pitch()};
    }
}
