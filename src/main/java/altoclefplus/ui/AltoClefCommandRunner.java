package altoclefplus.ui;

import adris.altoclef.AltoClef;
import altoclefplus.Plus;
import altoclefplus.core.logic.ItemInfo;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.inventory.logic.ArmorPlanner;
import altoclefplus.ui.logic.CommandRunner;
import altoclefplus.ui.logic.CommandSequence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs commands through AltoClef's executor directly (chat is never involved). ';' lists are run one part at
 * a time by {@link CommandSequence}, so Stop and Pause also stop the remaining parts. At the end, "get" parts
 * whose items were lost meanwhile (a death) are run again, and better armor that is only carried is put on.
 */
public final class AltoClefCommandRunner implements CommandRunner {
    private final AltoClef mod;
    private final CommandSequence sequence;

    public AltoClefCommandRunner(AltoClef mod) {
        this.mod = mod;
        this.sequence = new CommandSequence(this::executeOne, AltoClefCommandRunner::alreadyHave, 2, this::equipCarriedArmor);
    }

    @Override
    public void run(String command) {
        Plus.log().add("plus", "Running: " + command);
        sequence.run(command);
    }

    private void executeOne(String command, Runnable onFinish) {
        String prefix = mod.getModSettings().getCommandPrefix();
        String line = command.startsWith(prefix) ? command : prefix + command;
        AltoClef.getCommandExecutor().execute(line, onFinish, ex -> Plus.log().add("altoclef", ex.getMessage()));
    }

    /**
     * Is a "get" part still satisfied? Only equipment (tools, weapons, armor, shields) is judged: materials are
     * used up on purpose (ingots become a pickaxe), and anything we can't parse counts as done. Never throws.
     */
    static boolean alreadyHave(String command) {
        try {
            var wants = altoclefplus.ui.logic.GetCommand.parse(command);
            ClientPlayerEntity p = MinecraftClient.getInstance().player;
            if (wants == null || p == null) return true;
            for (var w : wants) {
                boolean equipment = !ItemInfo.toolType(w.id()).isEmpty() || !ItemInfo.armorSlot(w.id()).isEmpty()
                        || w.id().equals("shield");
                if (!equipment) continue;
                Identifier id = Identifier.tryParse("minecraft:" + w.id());
                Item item = id == null ? Items.AIR : Registry.ITEM.get(id);
                if (item == Items.AIR) continue;
                if (p.getInventory().count(item) < w.count()) return false;
            }
            return true;
        } catch (RuntimeException e) {
            return true;
        }
    }

    /** A preset that made armor should end with it worn, not left in the bag. */
    private void equipCarriedArmor() {
        if (!Plus.config().modules.inventory || !Plus.config().inventory.equipArmor) return;
        Snapshot s = Plus.snapshot();
        List<String> carried = new ArrayList<>();
        for (String id : s.items.keySet()) if (!ItemInfo.armorSlot(id).isEmpty()) carried.add(id);
        List<String> wear = new ArrayList<>();
        for (String slot : ItemInfo.ARMOR_SLOTS) {
            String best = ArmorPlanner.bestToWear(slot, s.armor.getOrDefault(slot, ""), carried, s.dimension, false);
            if (!best.isEmpty()) wear.add(best);
        }
        if (wear.isEmpty()) return;
        Plus.log().add("plus", "Putting on " + String.join(", ", wear));
        executeOne("equip [" + String.join(", ", wear) + "]", () -> { });
    }

    @Override
    public void cancel() {
        sequence.cancel();
        mod.getUserTaskChain().cancel(mod);
        mod.getClientBaritone().getPathingBehavior().cancelEverything();
        mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }
}
