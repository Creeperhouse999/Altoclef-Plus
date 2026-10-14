package altoclefplus.survival;

import adris.altoclef.AltoClef;
import altoclefplus.Plus;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.survival.logic.ProjectileMath;
import altoclefplus.survival.logic.ShieldRules;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Items;
import net.minecraft.tag.FluidTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Per-tick combat help on top of AltoClef's mob defense: keep a shield in the offhand and raise it against
 * incoming projectiles, aiming archers/blazes and fusing creepers; without a shield, sidestep projectiles
 * (only onto safe ground).
 */
public final class CombatAssist {
    private static final int HORIZON_TICKS = 20;
    private static final double HIT_RADIUS = 1.5;

    private boolean shieldUp;
    private int quietTicks;
    private int raisedTicks;
    private int dodgeTicks;
    private Input dodgeKey;
    private int offhandCheck;

    public void tick(AltoClef mod, PlusConfig.Survival cfg) {
        ClientPlayerEntity p = mod.getPlayer();
        if (p == null || p.world == null) return;
        if (dodgeTicks > 0 && --dodgeTicks == 0) releaseDodge(mod);
        if (cfg.shield && ++offhandCheck % 20 == 0) keepShieldInOffhand(p);

        Threat threat = scan(p);
        boolean hasShield = p.getOffHandStack().getItem() == Items.SHIELD;

        if (cfg.shield && hasShield && threat.any() && canRaise(p, mod)) {
            raise(mod);
        } else if (shieldUp) {
            quietTicks++;
            raisedTicks++;
            if (quietTicks >= 10 || raisedTicks >= 60 || !canRaise(p, mod)) lower(mod);
        }

        if (cfg.dodge && !hasShield && threat.projectile != null && dodgeTicks == 0 && p.isOnGround()) {
            dodge(mod, p, threat.projectile);
        }
    }

    public void stop(AltoClef mod) {
        if (shieldUp) lower(mod);
        if (dodgeTicks > 0) releaseDodge(mod);
    }

    private record Threat(Entity projectile, boolean other) {
        boolean any() {
            return projectile != null || other;
        }
    }

    private static Threat scan(ClientPlayerEntity p) {
        Vec3d centre = p.getBoundingBox().getCenter();
        Box area = p.getBoundingBox().expand(16);
        Entity incoming = null;
        boolean other = false;
        for (Entity e : p.world.getOtherEntities(p, area)) {
            if (e instanceof ProjectileEntity proj) {
                if (proj.getOwner() == p) continue;
                Vec3d v = e.getVelocity();
                if (v.lengthSquared() < 0.01) continue; // stuck in the ground
                double gravity = e instanceof ExplosiveProjectileEntity ? 0 : (e instanceof PersistentProjectileEntity ? 0.05 : 0.03);
                ProjectileMath.Approach a = ProjectileMath.closestApproach(e.getX(), e.getY(), e.getZ(), v.x, v.y, v.z,
                        centre.x, centre.y, centre.z, gravity, HORIZON_TICKS);
                if (a.distance() < HIT_RADIUS && a.ticks() > 0) incoming = e;
            } else if (e instanceof CreeperEntity c) {
                if (c.getFuseSpeed() > 0 && c.squaredDistanceTo(p) < 16) other = true;
            } else if (e instanceof AbstractSkeletonEntity || e instanceof PillagerEntity || e instanceof BlazeEntity) {
                var mob = (net.minecraft.entity.mob.MobEntity) e;
                if (mob.getTarget() == p && mob.canSee(p) && (mob.isAttacking() || mob.isUsingItem())) other = true;
            }
        }
        return new Threat(incoming, other);
    }

    /**
     * Holding right-click uses the main-hand item first and interacts with the crosshair target, so only raise
     * the shield when that can't do anything else (see ShieldRules).
     */
    private static boolean canRaise(ClientPlayerEntity p, AltoClef mod) {
        if (mod.getFoodChain().isTryingToEat() || MinecraftClient.getInstance().currentScreen != null) return false;
        String main = altoclefplus.core.StateSampler.id(p.getMainHandStack());
        return ShieldRules.canRaise(main, crosshair());
    }

    private static ShieldRules.Target crosshair() {
        HitResult hit = MinecraftClient.getInstance().crosshairTarget;
        if (hit == null || hit.getType() == HitResult.Type.MISS) return ShieldRules.Target.NOTHING;
        if (hit instanceof EntityHitResult e) {
            return e.getEntity() instanceof Monster ? ShieldRules.Target.HOSTILE : ShieldRules.Target.OTHER_ENTITY;
        }
        return ShieldRules.Target.BLOCK;
    }

    private void raise(AltoClef mod) {
        quietTicks = 0;
        if (!shieldUp) {
            shieldUp = true;
            raisedTicks = 0;
            mod.getInputControls().hold(Input.CLICK_RIGHT);
        }
    }

    private void lower(AltoClef mod) {
        shieldUp = false;
        mod.getInputControls().release(Input.CLICK_RIGHT);
    }

    private static void keepShieldInOffhand(ClientPlayerEntity p) {
        if (p.getOffHandStack().getItem() == Items.SHIELD || !p.getOffHandStack().isEmpty()) return;
        if (p.currentScreenHandler != p.playerScreenHandler || !p.currentScreenHandler.getCursorStack().isEmpty()) return;
        var main = p.getInventory().main;
        for (int i = 0; i < main.size(); i++) {
            if (main.get(i).getItem() == Items.SHIELD) {
                int window = i < 9 ? 36 + i : i;
                MinecraftClient.getInstance().interactionManager.clickSlot(p.playerScreenHandler.syncId, window, 40, SlotActionType.SWAP, p);
                Plus.log().add("survival", "Shield moved to offhand");
                return;
            }
        }
    }

    private void dodge(AltoClef mod, ClientPlayerEntity p, Entity projectile) {
        Vec3d v = projectile.getVelocity();
        int side = ProjectileMath.strafeSide(v.x, v.z, p.getX() - projectile.getX(), p.getZ() - projectile.getZ());
        double len = Math.max(1e-6, Math.hypot(v.x, v.z));
        double dx = side * v.z / len;
        double dz = side * -v.x / len;
        if (!safeToStep(p, dx, dz)) return;
        double yaw = Math.toRadians(p.getYaw());
        double rightX = -Math.cos(yaw);
        double rightZ = -Math.sin(yaw);
        dodgeKey = dx * rightX + dz * rightZ > 0 ? Input.MOVE_RIGHT : Input.MOVE_LEFT;
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(dodgeKey, true);
        dodgeTicks = 6;
    }

    private void releaseDodge(AltoClef mod) {
        if (dodgeKey != null) mod.getClientBaritone().getInputOverrideHandler().setInputForceState(dodgeKey, false);
        dodgeKey = null;
        dodgeTicks = 0;
    }

    /** Solid, non-lava ground 1 and 2 blocks to the side, and room to stand. */
    private static boolean safeToStep(ClientPlayerEntity p, double dx, double dz) {
        for (int dist = 1; dist <= 2; dist++) {
            BlockPos feet = new BlockPos(p.getX() + dx * dist, p.getY(), p.getZ() + dz * dist);
            BlockState below = p.world.getBlockState(feet.down());
            FluidState fluidFeet = p.world.getFluidState(feet);
            if (!below.isSolidBlock(p.world, feet.down()) || fluidFeet.isIn(FluidTags.LAVA)
                    || p.world.getFluidState(feet.down()).isIn(FluidTags.LAVA)
                    || p.world.getBlockState(feet).isSolidBlock(p.world, feet)) {
                return false;
            }
        }
        return true;
    }
}
