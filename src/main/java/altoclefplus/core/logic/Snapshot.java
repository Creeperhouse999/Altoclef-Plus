package altoclefplus.core.logic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable view of the game/bot state for one client tick. Built by StateSampler in-game and by
 * {@link #builder()} in tests. Item ids are the path part only ("iron_ingot").
 */
public final class Snapshot {
    public static final long NO_POS = Long.MIN_VALUE;

    public final long tick;
    public final double x, y, z;
    /** "overworld", "the_nether" or "the_end". */
    public final String dimension;
    public final float health;
    public final int food;
    /** Total count per item id across main inventory + hotbar + offhand (not worn armor). */
    public final Map<String, Integer> items;
    /** 9 entries, "" for empty. */
    public final List<String> hotbar;
    public final int selectedSlot;
    public final String offhand;
    /** Keys head/chest/legs/feet, value item id or "". */
    public final Map<String, String> armor;
    /** Lowest remaining durability fraction (0..1) per damageable item id. */
    public final Map<String, Float> durability;
    public final int freeSlots;
    public final boolean cursorEmpty;
    public final boolean screenOpen;
    public final boolean containerOpen;
    public final boolean userTaskRunning;
    public final String userCommand;
    /** Task chain from root to leaf, as AltoClef debug strings. */
    public final List<String> taskChain;
    public final String leafTask;
    public final boolean pathing;
    /** Increments every time Baritone starts a new path. */
    public final int pathCounter;
    /** Baritone goal is straight above/below (same x/z column) — pillaring is intended. */
    public final boolean goalVertical;
    public final boolean mobDefenseActive;
    public final boolean eating;
    public final boolean dead;
    public final boolean inLava;
    /** Packed block position currently being broken, or {@link #NO_POS}. */
    public final long breakingPos;
    public final int hostilesNear16;

    private Snapshot(Builder b) {
        tick = b.tick;
        x = b.x;
        y = b.y;
        z = b.z;
        dimension = b.dimension;
        health = b.health;
        food = b.food;
        items = Collections.unmodifiableMap(b.items);
        hotbar = List.copyOf(b.hotbar);
        selectedSlot = b.selectedSlot;
        offhand = b.offhand;
        armor = Collections.unmodifiableMap(b.armor);
        durability = Collections.unmodifiableMap(b.durability);
        freeSlots = b.freeSlots;
        cursorEmpty = b.cursorEmpty;
        screenOpen = b.screenOpen;
        containerOpen = b.containerOpen;
        userTaskRunning = b.userTaskRunning;
        userCommand = b.userCommand;
        taskChain = List.copyOf(b.taskChain);
        leafTask = b.leafTask;
        pathing = b.pathing;
        pathCounter = b.pathCounter;
        goalVertical = b.goalVertical;
        mobDefenseActive = b.mobDefenseActive;
        eating = b.eating;
        dead = b.dead;
        inLava = b.inLava;
        breakingPos = b.breakingPos;
        hostilesNear16 = b.hostilesNear16;
    }

    public int count(String itemId) {
        return items.getOrDefault(itemId, 0);
    }

    public boolean has(String itemId) {
        return count(itemId) > 0;
    }

    /** Seconds since tick 0 (20 ticks per second). */
    public double seconds() {
        return tick / 20.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.tick = tick;
        b.x = x;
        b.y = y;
        b.z = z;
        b.dimension = dimension;
        b.health = health;
        b.food = food;
        b.items = new java.util.HashMap<>(items);
        b.hotbar = new java.util.ArrayList<>(hotbar);
        b.selectedSlot = selectedSlot;
        b.offhand = offhand;
        b.armor = new java.util.HashMap<>(armor);
        b.durability = new java.util.HashMap<>(durability);
        b.freeSlots = freeSlots;
        b.cursorEmpty = cursorEmpty;
        b.screenOpen = screenOpen;
        b.containerOpen = containerOpen;
        b.userTaskRunning = userTaskRunning;
        b.userCommand = userCommand;
        b.taskChain = new java.util.ArrayList<>(taskChain);
        b.leafTask = leafTask;
        b.pathing = pathing;
        b.pathCounter = pathCounter;
        b.goalVertical = goalVertical;
        b.mobDefenseActive = mobDefenseActive;
        b.eating = eating;
        b.dead = dead;
        b.inLava = inLava;
        b.breakingPos = breakingPos;
        b.hostilesNear16 = hostilesNear16;
        return b;
    }

    public static final class Builder {
        private long tick;
        private double x, y = 64, z;
        private String dimension = "overworld";
        private float health = 20f;
        private int food = 20;
        private Map<String, Integer> items = new java.util.HashMap<>();
        private List<String> hotbar = new java.util.ArrayList<>(List.of("", "", "", "", "", "", "", "", ""));
        private int selectedSlot;
        private String offhand = "";
        private Map<String, String> armor = new java.util.HashMap<>(Map.of("head", "", "chest", "", "legs", "", "feet", ""));
        private Map<String, Float> durability = new java.util.HashMap<>();
        private int freeSlots = 36;
        private boolean cursorEmpty = true;
        private boolean screenOpen;
        private boolean containerOpen;
        private boolean userTaskRunning;
        private String userCommand = "";
        private List<String> taskChain = new java.util.ArrayList<>();
        private String leafTask = "";
        private boolean pathing;
        private int pathCounter;
        private boolean goalVertical;
        private boolean mobDefenseActive;
        private boolean eating;
        private boolean dead;
        private boolean inLava;
        private long breakingPos = NO_POS;
        private int hostilesNear16;

        public Builder tick(long v) { tick = v; return this; }
        public Builder pos(double px, double py, double pz) { x = px; y = py; z = pz; return this; }
        public Builder dimension(String v) { dimension = v; return this; }
        public Builder health(float v) { health = v; return this; }
        public Builder food(int v) { food = v; return this; }
        public Builder item(String id, int count) {
            if (count <= 0) items.remove(id); else items.put(id, count);
            return this;
        }
        public Builder items(Map<String, Integer> v) { items = new java.util.HashMap<>(v); return this; }
        public Builder hotbar(List<String> v) { hotbar = new java.util.ArrayList<>(v); return this; }
        public Builder hotbarSlot(int slot, String id) { hotbar.set(slot, id); return this; }
        public Builder selectedSlot(int v) { selectedSlot = v; return this; }
        public Builder offhand(String v) { offhand = v; return this; }
        public Builder armor(String slot, String id) { armor.put(slot, id); return this; }
        public Builder durability(String id, float fraction) { durability.put(id, fraction); return this; }
        public Builder freeSlots(int v) { freeSlots = v; return this; }
        public Builder cursorEmpty(boolean v) { cursorEmpty = v; return this; }
        public Builder screenOpen(boolean v) { screenOpen = v; return this; }
        public Builder containerOpen(boolean v) { containerOpen = v; return this; }
        public Builder userTaskRunning(boolean v) { userTaskRunning = v; return this; }
        public Builder userCommand(String v) { userCommand = v; return this; }
        public Builder taskChain(List<String> v) { taskChain = new java.util.ArrayList<>(v); return this; }
        public Builder leafTask(String v) { leafTask = v; return this; }
        public Builder pathing(boolean v) { pathing = v; return this; }
        public Builder pathCounter(int v) { pathCounter = v; return this; }
        public Builder goalVertical(boolean v) { goalVertical = v; return this; }
        public Builder mobDefenseActive(boolean v) { mobDefenseActive = v; return this; }
        public Builder eating(boolean v) { eating = v; return this; }
        public Builder dead(boolean v) { dead = v; return this; }
        public Builder inLava(boolean v) { inLava = v; return this; }
        public Builder breakingPos(long v) { breakingPos = v; return this; }
        public Builder hostilesNear16(int v) { hostilesNear16 = v; return this; }

        public Snapshot build() {
            return new Snapshot(this);
        }
    }
}
