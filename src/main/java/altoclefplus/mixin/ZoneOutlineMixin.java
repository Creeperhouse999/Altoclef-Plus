package altoclefplus.mixin;

import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import altoclefplus.zones.Zones;
import altoclefplus.zones.logic.Zone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws safe-zone outlines in the world when "Show zones" is on. */
@Mixin(WorldRenderer.class)
public abstract class ZoneOutlineMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void altoclefplus$drawZones(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
                                        Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmap,
                                        Matrix4f projection, CallbackInfo ci) {
        Hooks.hit("zone-outline");
        if (Plus.mod() == null || !Plus.config().ui.showZones) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || Zones.current().zones().isEmpty()) return;
        Vec3d cam = camera.getPos();
        VertexConsumerProvider.Immediate buffers = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer lines = buffers.getBuffer(RenderLayer.getLines());
        int bottom = client.world.getBottomY();
        int top = client.world.getTopY();
        for (Zone z : Zones.current().zones()) {
            double minY = z.fullHeight ? Math.max(bottom, cam.y - 48) : z.minY;
            double maxY = z.fullHeight ? Math.min(top, cam.y + 48) : z.maxY + 1;
            Box box = new Box(z.minX, minY, z.minZ, z.maxX + 1, maxY, z.maxZ + 1).offset(-cam.x, -cam.y, -cam.z);
            WorldRenderer.drawBox(matrices, lines, box, 0.35f, 0.9f, 0.45f, 1.0f);
        }
        buffers.draw(RenderLayer.getLines());
    }
}
