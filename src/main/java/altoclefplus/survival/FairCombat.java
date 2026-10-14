package altoclefplus.survival;

import altoclefplus.Plus;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.survival.logic.CombatAim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Gate in front of AltoClef's melee attack: no hits through blocks, and never while facing away. If the bot
 * is not facing the target, it turns to it and sends that rotation to the server before the hit. (Waiting a
 * tick would not work: Baritone re-applies its own rotation every tick while walking.)
 */
public final class FairCombat {
    private FairCombat() {}

    /** @return true if the attack may go ahead (after turning to face the target if needed). */
    public static boolean allow(Entity target) {
        PlusConfig.Survival cfg = Plus.config().survival;
        if (Plus.mod() == null || !Plus.config().modules.survival || !cfg.fairCombat) return true;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || target == null) return true;

        Vec3d eye = player.getEyePos();
        Vec3d centre = target.getBoundingBox().getCenter();
        Vec3d head = target.getEyePos();
        boolean sees = clear(eye, centre, player) || clear(eye, head, player);
        if (!sees) return false;

        Vec3d look = player.getRotationVec(1f);
        Vec3d dir = centre.subtract(eye);
        double angle = CombatAim.angleTo(look.x, look.y, look.z, dir.x, dir.y, dir.z);
        if (CombatAim.mayHit(true, angle, cfg.fairCombatMaxAngle)) return true;

        float[] a = CombatAim.lookAngles(eye.x, eye.y, eye.z, centre.x, centre.y, centre.z);
        player.setYaw(a[0]);
        player.setPitch(a[1]);
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(a[0], a[1], player.isOnGround()));
        }
        return true;
    }

    private static boolean clear(Vec3d from, Vec3d to, Entity viewer) {
        HitResult hit = viewer.world.raycast(new RaycastContext(from, to,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, viewer));
        return hit.getType() == HitResult.Type.MISS;
    }
}
