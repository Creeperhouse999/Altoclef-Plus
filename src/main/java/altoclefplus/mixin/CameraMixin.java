package altoclefplus.mixin;

import altoclefplus.camera.CameraModule;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Smooths only the rendered view. The first setRotation call in Camera.update copies the entity's angles;
 * later calls (front third-person view, sleeping) derive from the camera's own angles, so they inherit the
 * smoothing automatically.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Redirect(method = "update", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0))
    private void altoclefplus$smoothRotation(Camera self, float yaw, float pitch) {
        float[] r = CameraModule.apply(yaw, pitch);
        setRotation(r[0], r[1]);
    }
}
