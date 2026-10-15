package altoclefplus.zones;

import altoclefplus.Plus;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.zones.logic.AttackRules;
import altoclefplus.zones.logic.BreakRules;
import altoclefplus.zones.logic.MobCategory;
import altoclefplus.zones.logic.ZoneSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Live safe zones and break/attack rules for the current world. Zones also feed Baritone's own "avoid
 * breaking / placing / walking here" lists so paths are planned around them.
 */
public final class Zones {
    private static volatile ZoneSet current = ZoneSet.empty();
    private static volatile BreakRules breakRules = new BreakRules(java.util.Set.of());
    private static volatile boolean nothingToCheck = true;
    private static String currentWorld = "";
    private static int zonesHash;

    private Zones() {}

    /**
     * Hook zones into Baritone's planning. Called once at startup. Registered in AltoClef's base behaviour
     * state: AltoClef rewrites Baritone's avoid lists from its state stack on every push/pop, so rules added
     * to AltoClefSettings directly were wiped as soon as the first task started.
     */
    public static void registerWithBaritone(adris.altoclef.AltoClef mod) {
        var b = mod.getBehaviour();
        // AltoClef adds its protected areas straight to Baritone (wiped by the state stack): keep them here too.
        b.avoidBlockBreaking(pos -> mod.getModSettings() != null && mod.getModSettings().isPositionExplicitelyProtected(pos));
        b.avoidBlockPlacing(pos -> mod.getModSettings() != null && mod.getModSettings().isPositionExplicitelyProtected(pos));
        b.avoidBlockBreaking(pos -> enabled() && !mayBreak(pos));
        b.avoidBlockPlacing(pos -> enabled() && current.forbids(ZoneSet.Action.PLACE, pos.getX(), pos.getY(), pos.getZ()));
        b.avoidWalkingThrough(pos -> enabled()
                && current.forbids(ZoneSet.Action.ENTER, pos.getX(), pos.getY(), pos.getZ()));
    }

    /** Rebuild the zone set when the world or the zone list changed. Cheap; called every tick. */
    /**
     * Blocks that hold an item frame or painting. Breaking them pops the frame off, and frames label chests,
     * so they are never broken. Refreshed on the game thread; read by Baritone's pathing thread.
     */
    private static volatile Set<Long> frameSupports = Set.of();
    private static int frameScanTick;

    public static void refresh() {
        if (++frameScanTick % 20 == 0) scanFrames();
        var deny = Plus.config().rules.neverBreakBlocks;
        breakRules = new BreakRules(new HashSet<>(deny));
        String world = worldKey();
        List<altoclefplus.zones.logic.Zone> all = Plus.config().zones;
        int hash = System.identityHashCode(all) * 31 + all.size();
        if (!world.equals(currentWorld) || hash != zonesHash || current.zones().size() != countFor(all, world)) {
            current = new ZoneSet(all, world);
            currentWorld = world;
            zonesHash = hash;
        }
        nothingToCheck = current.zones().isEmpty() && deny.isEmpty();
    }

    private static void scanFrames() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            frameSupports = Set.of();
            return;
        }
        Set<Long> supports = new HashSet<>();
        for (var e : client.world.getEntitiesByClass(net.minecraft.entity.decoration.AbstractDecorationEntity.class,
                client.player.getBoundingBox().expand(96), x -> true)) {
            supports.add(e.getDecorationBlockPos().offset(e.getHorizontalFacing().getOpposite()).asLong());
        }
        frameSupports = supports;
    }

    /** Force a rebuild (after editing a zone's corners or rules). */
    public static void invalidate() {
        zonesHash = 0;
        currentWorld = "";
    }

    private static int countFor(List<altoclefplus.zones.logic.Zone> all, String world) {
        int n = 0;
        for (var z : all) if (world.equals(z.world)) n++;
        return n;
    }

    public static ZoneSet current() {
        return current;
    }

    /** Rules apply while the bot is running a task (never to your own clicks). */
    public static boolean botActive() {
        return Plus.mod() != null && Plus.snapshot().userTaskRunning;
    }

    static boolean enabled() {
        return Plus.mod() != null;
    }

    /** Called by Baritone for every block it considers, so it must stay cheap. */
    public static boolean mayBreak(BlockPos pos) {
        if (!frameSupports.isEmpty() && frameSupports.contains(pos.asLong())) return false;
        if (nothingToCheck) return true;
        MinecraftClient client = MinecraftClient.getInstance();
        String blockId = client.world == null ? "" : Registry.BLOCK.getId(client.world.getBlockState(pos).getBlock()).getPath();
        return breakRules.mayBreak(blockId, current, pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean mayPlace(BlockPos pos) {
        return !current.forbids(ZoneSet.Action.PLACE, pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean mayLoot(BlockPos pos) {
        return !current.forbids(ZoneSet.Action.LOOT, pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean mayAttack(Entity target) {
        // Item frames and paintings are never hit (frames label chests; hitting one takes its item out).
        if (target instanceof net.minecraft.entity.decoration.AbstractDecorationEntity) return false;
        PlusConfig.Rules r = Plus.config().rules;
        if (r.neverAttackPlayers && target instanceof net.minecraft.entity.player.PlayerEntity && !playerCommandRunning()) return false;
        AttackRules rules = new AttackRules(r.neverAttackNamed, r.neverAttackTamed, new HashSet<>(r.neverAttackMobs), r.selfDefense);
        var player = MinecraftClient.getInstance().player;
        boolean attackingMe = player != null && target instanceof MobEntity mob && mob.getTarget() == player;
        String type = Registry.ENTITY_TYPE.getId(target.getType()).getPath();
        return rules.mayAttack(category(target), type, target.hasCustomName(), isTamed(target), attackingMe,
                current, target.getX(), target.getY(), target.getZ());
    }

    /** You asked for it yourself ("punk <player>" / "terminate"): the no-PvP rule doesn't stall that task. */
    private static boolean playerCommandRunning() {
        String cmd = Plus.control() == null ? "" : Plus.control().lastCommand().trim().toLowerCase(java.util.Locale.ROOT);
        return cmd.startsWith("punk") || cmd.startsWith("terminate");
    }

    public static MobCategory category(Entity e) {
        if (e instanceof Angerable || e instanceof AbstractPiglinEntity) return MobCategory.NEUTRAL;
        if (e instanceof Monster) return MobCategory.HOSTILE;
        return MobCategory.FRIENDLY;
    }

    private static boolean isTamed(Entity e) {
        if (e instanceof TameableEntity t) return t.isTamed();
        if (e instanceof HorseBaseEntity h) return h.isTame();
        return false;
    }

    /** Singleplayer save folder name, or the server address. */
    public static String worldKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        IntegratedServer server = client.getServer();
        if (server != null) {
            try {
                return "sp:" + server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().normalize().getFileName();
            } catch (RuntimeException e) {
                return "sp:" + server.getSaveProperties().getLevelName();
            }
        }
        ServerInfo info = client.getCurrentServerEntry();
        return info != null ? "mp:" + info.address : "";
    }
}
