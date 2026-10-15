package altoclefplus.core;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskChain;
import altoclefplus.core.logic.Snapshot;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalTwoBlocks;
import baritone.api.pathing.goals.GoalYLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Builds a {@link Snapshot} from the live game once per client tick. */
public final class StateSampler {
    private static final String[] ARMOR_KEYS = {"feet", "legs", "chest", "head"};

    private final Supplier<String> userCommand;
    private long tick;
    private IPath lastPath;
    private int pathCounter;
    private long breakingPos = Snapshot.NO_POS;
    private long breakingTick = -10;

    public StateSampler(Supplier<String> userCommand) {
        this.userCommand = userCommand;
        EventBus.subscribe(BlockBreakingEvent.class, evt -> {
            breakingPos = evt.blockPos.asLong();
            breakingTick = tick;
        });
        EventBus.subscribe(BlockBreakingCancelEvent.class, evt -> breakingPos = Snapshot.NO_POS);
    }

    public static String id(ItemStack stack) {
        return stack.isEmpty() ? "" : Registry.ITEM.getId(stack.getItem()).getPath();
    }

    public Snapshot sample(AltoClef mod) {
        tick++;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        Snapshot.Builder b = Snapshot.builder().tick(tick);
        if (player == null || client.world == null) {
            return b.build();
        }

        b.pos(player.getX(), player.getY(), player.getZ())
                .dimension(client.world.getRegistryKey().getValue().getPath())
                .health(player.getHealth())
                .food(player.getHungerManager().getFoodLevel())
                .dead(player.isDead() || client.currentScreen instanceof DeathScreen)
                .inLava(player.isInLava())
                .screenOpen(client.currentScreen != null)
                .paused(client.isPaused())
                .containerOpen(client.currentScreen instanceof HandledScreen && !(client.currentScreen instanceof InventoryScreen))
                .cursorEmpty(player.currentScreenHandler == null || player.currentScreenHandler.getCursorStack().isEmpty());

        sampleInventory(player.getInventory(), b);
        sampleTasks(mod, b);
        sampleBaritone(mod, b);

        if (tick - breakingTick <= 2) b.breakingPos(breakingPos);
        b.hostilesNear16(client.world.getEntitiesByClass(HostileEntity.class,
                player.getBoundingBox().expand(16), e -> e.isAlive()).size());
        return b.build();
    }

    private static void sampleInventory(PlayerInventory inv, Snapshot.Builder b) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, Float> durability = new HashMap<>();
        List<String> hotbar = new ArrayList<>();
        int free = 0;
        for (int i = 0; i < inv.main.size(); i++) {
            ItemStack s = inv.main.get(i);
            if (i < 9) hotbar.add(id(s));
            if (s.isEmpty()) {
                free++;
                continue;
            }
            count(s, counts, durability);
        }
        ItemStack off = inv.offHand.get(0);
        if (!off.isEmpty()) count(off, counts, durability);
        for (int i = 0; i < 4; i++) {
            ItemStack a = inv.armor.get(i);
            b.armor(ARMOR_KEYS[i], id(a));
        }
        b.items(counts).hotbar(hotbar).freeSlots(free).selectedSlot(inv.selectedSlot).offhand(id(off));
        durability.forEach(b::durability);
    }

    private static void count(ItemStack s, Map<String, Integer> counts, Map<String, Float> durability) {
        String id = id(s);
        counts.merge(id, s.getCount(), Integer::sum);
        if (s.isDamageable()) {
            float left = 1f - (float) s.getDamage() / s.getMaxDamage();
            durability.merge(id, left, Math::min);
        }
    }

    private void sampleTasks(AltoClef mod, Snapshot.Builder b) {
        boolean running = mod.getUserTaskChain().isActive() && !mod.getUserTaskChain().isRunningIdleTask();
        b.userTaskRunning(running).userCommand(userCommand.get());
        TaskChain current = mod.getTaskRunner().getCurrentTaskChain();
        List<String> chain = new ArrayList<>();
        String leaf = "";
        if (current != null) {
            for (Task t : current.getTasks()) {
                chain.add(t.toString());
                leaf = t.getClass().getSimpleName();
            }
        }
        b.taskChain(chain).leafTask(leaf)
                .mobDefenseActive(current == mod.getMobDefenseChain())
                .eating(mod.getFoodChain().isTryingToEat());
    }

    private void sampleBaritone(AltoClef mod, Snapshot.Builder b) {
        var pathing = mod.getClientBaritone().getPathingBehavior();
        IPath path = pathing.getPath().orElse(null);
        if (path != null && path != lastPath) pathCounter++;
        lastPath = path;
        b.pathing(pathing.isPathing()).pathCounter(pathCounter);

        Goal goal = pathing.getGoal();
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        boolean vertical = goal instanceof GoalYLevel;
        BlockPos target = null;
        if (goal instanceof GoalBlock g) target = g.getGoalPos();
        else if (goal instanceof GoalGetToBlock g) target = g.getGoalPos();
        else if (goal instanceof GoalTwoBlocks g) target = g.getGoalPos();
        if (target != null && p != null) {
            vertical = Math.abs(target.getX() - p.getBlockX()) <= 1 && Math.abs(target.getZ() - p.getBlockZ()) <= 1;
        }
        b.goalVertical(vertical);
    }
}
