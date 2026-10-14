package altoclefplus.inventory;

import altoclefplus.Plus;
import altoclefplus.core.StateSampler;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.inventory.logic.HotbarLayout;
import altoclefplus.inventory.logic.ToolChoice;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Replaces AltoClef's "swap whatever I need into the selected slot" with: select it if it's already on the
 * hotbar, otherwise swap it into its own slot. Also picks the right tool on every block-break tick.
 * Only acts on the player's own inventory (never while a container is open).
 */
public final class HotbarManager {
    private static HotbarLayout layout;
    private static long tick;

    private HotbarManager() {}

    public static void onTick() {
        tick++;
    }

    private static HotbarLayout layout() {
        PlusConfig.Inventory cfg = Plus.config().inventory;
        if (layout == null) {
            layout = new HotbarLayout(cfg.hotbarLayout, new HashSet<>(cfg.buildingBlocks), cfg.hotbarHysteresisSeconds * 20);
        }
        return layout;
    }

    /** Layout settings changed in the panel. */
    public static void invalidate() {
        layout = null;
    }

    static boolean enabled() {
        return Plus.mod() != null && Plus.config().modules.inventory && Plus.config().inventory.fixHotbar;
    }

    private static ClientPlayerEntity ownInventoryPlayer() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null || MinecraftClient.getInstance().interactionManager == null) return null;
        if (p.currentScreenHandler != p.playerScreenHandler) return null;
        if (!p.currentScreenHandler.getCursorStack().isEmpty()) return null;
        return p;
    }

    /** Equip the stack at a PlayerInventory index (0-8 hotbar, 9-35 main). @return false if not handled. */
    public static boolean equipInventorySlot(int invIndex) {
        ClientPlayerEntity p = ownInventoryPlayer();
        if (p == null || invIndex < 0 || invIndex > 35) return false;
        PlayerInventory inv = p.getInventory();
        if (invIndex < 9) {
            inv.selectedSlot = invIndex;
            return true;
        }
        String id = StateSampler.id(inv.main.get(invIndex));
        if (id.isEmpty()) return false;
        List<String> bar = hotbarIds(inv);
        HotbarLayout.Plan plan = layout().plan(bar, id, tick);
        int target = plan.slot();
        // Window slot index of main inventory slot i (9-35) in the player screen is i itself.
        MinecraftClient.getInstance().interactionManager.clickSlot(p.playerScreenHandler.syncId, invIndex, target, SlotActionType.SWAP, p);
        layout().recordMove(target, tick);
        inv.selectedSlot = target;
        return true;
    }

    /** Equip an item by type. @return false if not carried or not handled. */
    public static boolean equipItem(Item item) {
        ClientPlayerEntity p = ownInventoryPlayer();
        if (p == null) return false;
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.main.get(i).getItem() == item) {
                inv.selectedSlot = i;
                return true;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (inv.main.get(i).getItem() == item) return equipInventorySlot(i);
        }
        return false;
    }

    /** Called at the start of every block-break tick: hold the best tool, never armor or blocks. */
    public static void beforeBreak(BlockPos pos) {
        if (Plus.mod() == null || !Plus.config().modules.inventory || !Plus.config().inventory.fixTools) return;
        if (!Plus.snapshot().userTaskRunning && !Plus.snapshot().pathing) return;
        ClientPlayerEntity p = ownInventoryPlayer();
        if (p == null) return;
        BlockState state = p.world.getBlockState(pos);
        PlayerInventory inv = p.getInventory();

        List<ToolChoice.Candidate> hotbar = new ArrayList<>();
        float bestHotbar = 1f;
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.main.get(i);
            float speed = s.isEmpty() ? 1f : s.getMiningSpeedMultiplier(state);
            hotbar.add(new ToolChoice.Candidate(i, StateSampler.id(s), speed));
            if (isTool(s)) bestHotbar = Math.max(bestHotbar, speed);
        }
        int bestMain = -1;
        float bestMainSpeed = bestHotbar;
        for (int i = 9; i < 36; i++) {
            ItemStack s = inv.main.get(i);
            if (!isTool(s)) continue;
            float speed = s.getMiningSpeedMultiplier(state);
            if (speed > bestMainSpeed) {
                bestMain = i;
                bestMainSpeed = speed;
            }
        }
        if (bestMain >= 0) {
            equipInventorySlot(bestMain);
            return;
        }
        PlusConfig.Inventory cfg = Plus.config().inventory;
        Set<String> junk = new HashSet<>(cfg.junkItems);
        junk.addAll(cfg.buildingBlocks);
        int slot = ToolChoice.pick(hotbar, new HashSet<>(cfg.protectedItems), junk);
        if (slot >= 0) inv.selectedSlot = slot;
    }

    private static boolean isTool(ItemStack s) {
        String id = StateSampler.id(s);
        return !altoclefplus.core.logic.ItemInfo.toolType(id).isEmpty() || id.equals("shears");
    }

    private static List<String> hotbarIds(PlayerInventory inv) {
        List<String> bar = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) bar.add(StateSampler.id(inv.main.get(i)));
        return bar;
    }
}
