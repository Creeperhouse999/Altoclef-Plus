package altoclefplus.inventory;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.SlotClickChangedEvent;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasksystem.Task;
import altoclefplus.Plus;
import altoclefplus.core.PlusChain;
import altoclefplus.core.StateSampler;
import altoclefplus.core.logic.ItemInfo;
import altoclefplus.core.logic.Module;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.core.logic.events.Notice;
import altoclefplus.inventory.logic.ArmorPlanner;
import altoclefplus.inventory.logic.CursorGuard;
import altoclefplus.inventory.logic.DoorPlanner;
import altoclefplus.inventory.logic.JunkClassifier;
import altoclefplus.inventory.logic.SpareToolPlanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Armor, piecewise armor crafting, spare tools, junk disposal and the cursor guard. */
public final class InventoryModule implements Module {
    private static final int DECIDE_EVERY_TICKS = 20;

    private final PlusChain chain;
    private final CursorGuard cursorGuard = new CursorGuard(100);
    private boolean clickedThisTick;
    private long ticks;
    private String status = "idle";

    public InventoryModule(AltoClef mod) {
        chain = new PlusChain(mod.getTaskRunner(), "AltoClef Plus inventory", 52f, 90_000);
        EventBus.subscribe(SlotClickChangedEvent.class, e -> clickedThisTick = true);
    }

    @Override
    public String id() {
        return "inventory";
    }

    @Override
    public void tick(Snapshot s) {
        boolean clicked = clickedThisTick;
        clickedThisTick = false;
        PlusConfig.Inventory cfg = Plus.config().inventory;

        if (cfg.cursorGuard && cursorGuard.tick(s.cursorEmpty, clicked, s.containerOpen) == CursorGuard.Action.STASH) {
            if (s.userTaskRunning) chain.startJob(new CursorStashTask(), "Putting away a stuck cursor item", null);
        }
        if (++ticks % DECIDE_EVERY_TICKS != 0) return;
        if (!s.userTaskRunning || s.mobDefenseActive || s.eating || s.screenOpen || s.dead || "the_end".equals(s.dimension)) return;
        if (chain.isActive()) {
            status = chain.jobLabel();
            return;
        }
        status = "idle";
        // Never in the middle of placing a table, crafting or using a furnace: AltoClef would start over.
        if (altoclefplus.core.logic.TaskChainState.inDelicateStep(s.taskChain)) return;

        if (cfg.equipArmor && tryEquipArmor(s)) return;
        if ((cfg.dropJunk || cfg.dropOutdatedTools) && tryDropJunk(s, cfg)) return;
        if (cfg.craftArmorPiecewise) {
            Optional<String> piece = ArmorPlanner.nextPiece(s);
            if (piece.isPresent() && start(TaskCatalogue.getItemTask(piece.get(), 1), "Crafting " + pretty(piece.get()))) return;
        }
        if (Plus.config().survival.carryDoor && DoorPlanner.shouldCraft(s)
                && start(TaskCatalogue.getItemTask("wooden_door", 1), "Making a door for underwater air pockets")) return;
        if (cfg.spareTools) {
            Optional<String> tool = SpareToolPlanner.next(s);
            if (tool.isPresent()) start(TaskCatalogue.getItemTask(tool.get(), 1), "Making a spare " + pretty(tool.get()));
        }
    }

    private boolean start(Task task, String label) {
        if (task == null) return false;
        status = label;
        return chain.startJob(task, label, null);
    }

    private boolean tryEquipArmor(Snapshot s) {
        List<String> carried = new ArrayList<>();
        for (String id : s.items.keySet()) if (!ItemInfo.armorSlot(id).isEmpty()) carried.add(id);
        if (carried.isEmpty()) return false;
        for (String slot : ItemInfo.ARMOR_SLOTS) {
            String wear = ArmorPlanner.bestToWear(slot, s.armor.getOrDefault(slot, ""), carried, s.dimension);
            if (!wear.isEmpty()) {
                Item item = Registry.ITEM.get(new Identifier("minecraft", wear));
                return start(new EquipArmorTask(item), "Putting on " + pretty(wear));
            }
        }
        return false;
    }

    /** Drops one junk stack per decision, straight from the player inventory. */
    private boolean tryDropJunk(Snapshot s, PlusConfig.Inventory cfg) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null || p.currentScreenHandler != p.playerScreenHandler || !s.cursorEmpty) return false;
        Set<String> keep = new HashSet<>(cfg.protectedItems);
        if (Plus.config().survival.carryDoor) keep.addAll(DoorPlanner.WOODEN_DOORS);
        JunkClassifier classifier = new JunkClassifier(keep, new HashSet<>(cfg.junkItems),
                new HashSet<>(cfg.buildingBlocks), cfg.buildingBlockCapPerType, cfg.buildingBlockCapTotal, cfg.junkFreeSlotThreshold);
        Set<String> needed = altoclefplus.core.TaskTargets.of(Plus.mod());
        List<JunkClassifier.Drop> drops = new ArrayList<>();
        if (cfg.dropOutdatedTools) drops.addAll(JunkClassifier.outdatedTools(s.items, needed, keep));
        if (cfg.dropJunk) drops.addAll(classifier.toDrop(s.items, needed, s.freeSlots));
        for (JunkClassifier.Drop d : drops) {
            Item item = Registry.ITEM.get(new Identifier("minecraft", d.id()));
            if (Plus.mod().getModSettings().isImportant(item)) continue;
            int slot = findDroppableSlot(p, d.id(), s.selectedSlot);
            if (slot < 0) continue;
            int window = slot < 9 ? 36 + slot : slot;
            MinecraftClient.getInstance().interactionManager.clickSlot(p.playerScreenHandler.syncId, window, 1, SlotActionType.THROW, p);
            Plus.bus().publish(new Notice("inventory", "Dropped junk: " + pretty(d.id())));
            return true;
        }
        return false;
    }

    /** Main inventory first, never the held slot, never named or enchanted stacks. */
    private static int findDroppableSlot(ClientPlayerEntity p, String id, int selected) {
        var main = p.getInventory().main;
        for (int pass = 0; pass < 2; pass++) {
            for (int i = pass == 0 ? 9 : 0; i < (pass == 0 ? 36 : 9); i++) {
                if (i == selected) continue;
                ItemStack st = main.get(i);
                if (StateSampler.id(st).equals(id) && !st.hasCustomName() && !st.hasEnchantments()) return i;
            }
        }
        return -1;
    }

    private static String pretty(String id) {
        return id.replace('_', ' ');
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public void onDisable() {
        chain.cancelJob();
    }
}
