package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.WorldHelper;
import altoclefplus.Plus;
import altoclefplus.inventory.HotbarManager;
import altoclefplus.survival.logic.CritGate;
import altoclefplus.survival.logic.CritTiming;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Comparator;

/** Crit timing and weapon choice for every AltoClef melee hit (called from mixins). */
public final class MeleeAssist {
    private static final CritGate GATE = new CritGate(15);
    private static final altoclefplus.survival.logic.WeaponHold HOLD = new altoclefplus.survival.logic.WeaponHold(20);

    private MeleeAssist() {}

    /** Called right before AltoClef swings at {@code target}. False = don't swing this tick. */
    public static boolean allowSwing(Entity target) {
        if (!Plus.config().survival.critHits || !(target instanceof LivingEntity)) return true;
        AltoClef mod = Plus.mod();
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (mod == null || p == null) return true;
        // While Baritone walks us it replaces the movement input and ignores the jump key: no crits then.
        boolean safe = !mod.getClientBaritone().getPathingBehavior().isPathing() && CritTiming.canCrit(p.isTouchingWater(), p.isInLava(), p.isClimbing(), p.hasVehicle(),
                p.hasStatusEffect(StatusEffects.LEVITATION) || p.hasStatusEffect(StatusEffects.SLOW_FALLING),
                WorldHelper.isAir(mod, p.getBlockPos().up(2)));
        switch (GATE.decide(p.getAttackCooldownProgress(0f), p.isOnGround(), p.getVelocity().y, safe, p.age)) {
            case JUMP -> {
                mod.getInputControls().tryPress(Input.JUMP);
                return false;
            }
            case WAIT -> {
                return false;
            }
            default -> {
                if (!p.isOnGround()) p.setSprinting(false); // sprinting cancels the critical hit
                return true;
            }
        }
    }

    /** Equips the situational weapon. True if handled (AltoClef's own choice is skipped). */
    public static boolean equipWeapon() {
        if (!Plus.config().survival.smartWeapon) return false;
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null || p.world == null) return false;
        var near = p.world.getEntitiesByClass(HostileEntity.class, p.getBoundingBox().expand(4), Entity::isAlive);
        LivingEntity target = near.stream().min(Comparator.comparingDouble(p::squaredDistanceTo)).orElse(null);
        var items = Plus.snapshot().items;
        String fresh = CritTiming.chooseWeapon(items, near.size(),
                target != null && target.isBlocking(), target == null ? 20f : target.getHealth());
        if (fresh.isEmpty()) return false;
        String id = HOLD.choose(fresh, p.age, items::containsKey);
        Item item = Registry.ITEM.get(new Identifier("minecraft", id));
        return item != Items.AIR && HotbarManager.equipItem(item);
    }
}
