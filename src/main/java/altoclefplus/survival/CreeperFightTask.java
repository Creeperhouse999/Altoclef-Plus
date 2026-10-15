package altoclefplus.survival;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Hand;

/**
 * Kills a creeper that keeps following: walk up, hit with a full charge bar (the knockback also stops its fuse),
 * then step back while the bar refills. If it starts hissing up close, AltoClef's run-away (higher priority)
 * takes over. Gives up after 20 seconds.
 */
public final class CreeperFightTask extends Task {
    private static final double REACH = 3.0;
    private final CreeperEntity creeper;
    private final TimerGame timeout = new TimerGame(20);
    private int backOffTicks;

    public CreeperFightTask(CreeperEntity creeper) {
        this.creeper = creeper;
    }

    @Override
    protected void onStart(AltoClef mod) {
        timeout.reset();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        ClientPlayerEntity p = mod.getPlayer();
        if (backOffTicks > 0) {
            backOffTicks--;
            setDebugState("Stepping back from the creeper");
            LookHelper.lookAt(mod, creeper.getEyePos());
            mod.getInputControls().hold(Input.MOVE_BACK);
            return null;
        }
        mod.getInputControls().release(Input.MOVE_BACK);
        double dist = p.distanceTo(creeper);
        if (dist > REACH) {
            setDebugState("Going for the creeper");
            return new GetToEntityTask(creeper, REACH - 0.5);
        }
        MeleeAssist.equipWeapon();
        LookHelper.lookAt(mod, creeper.getBoundingBox().getCenter());
        if (p.getAttackCooldownProgress(0f) >= 0.95f) {
            setDebugState("Hitting the creeper");
            MinecraftClient.getInstance().interactionManager.attackEntity(p, creeper);
            p.swingHand(Hand.MAIN_HAND);
            backOffTicks = 8;
        } else {
            setDebugState("Waiting for the charge bar");
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getInputControls().release(Input.MOVE_BACK);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return !creeper.isAlive() || creeper.isRemoved() || timeout.elapsed() || !altoclefplus.zones.Zones.mayAttack(creeper)
                || mod.getPlayer() == null || mod.getPlayer().getHealth() < 8;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof CreeperFightTask c && c.creeper == creeper;
    }

    @Override
    protected String toDebugString() {
        return "Fighting a creeper";
    }
}
