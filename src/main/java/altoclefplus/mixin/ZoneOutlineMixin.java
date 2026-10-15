package altoclefplus.mixin;

import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** World rendering: our overlay (path, blocks, goal, mobs) and safe-zone outlines when "Show zones" is on. */
@Mixin(WorldRenderer.class)
public abstract class ZoneOutlineMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void altoclefplus$drawZones(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
                                        Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmap,
                                        Matrix4f projection, CallbackInfo ci) {
        Hooks.hit("zone-outline");
        if (Plus.mod() == null) return;
        try {
            altoclefplus.ui.WorldOverlay.render(matrices, camera, tickDelta);
            altoclefplus.ui.WorldOverlay.renderZones(matrices, camera);
        } catch (RuntimeException ignored) {
            // an overlay glitch must never break world rendering
        }
    }
}
