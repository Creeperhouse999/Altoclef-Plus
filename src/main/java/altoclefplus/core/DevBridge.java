package altoclefplus.core;

import altoclefplus.Plus;
import altoclefplus.core.logic.LogBuffer;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.ui.logic.TaskNarrator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.TranslatableText;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Developer test bridge, off unless {@code config/altoclefplus-dev/enabled} exists. Lets a test script drive
 * the game without touching the window (it can sit on another desktop): commands are read from
 * {@code in.txt}, replies go to {@code out.txt}, and {@code status.txt} / {@code log.txt} are refreshed every
 * second. Screenshots use Minecraft's own screenshot code, so they work while the window is hidden.
 * <p>
 * Commands: {@code run <altoclef command>}, {@code stop}, {@code pause}, {@code chat <text or /command>},
 * {@code shot <name>}, {@code world <save folder>}, {@code quit} (save and quit to title), {@code exit}.
 */
public final class DevBridge {
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("altoclefplus-dev");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static Boolean enabled;
    private static long ticks;

    private DevBridge() {}

    public static void tick() {
        if (enabled == null) enabled = Files.exists(DIR.resolve("enabled"));
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        // The window may be on another desktop: never auto-pause because it lost focus.
        client.options.pauseOnLostFocus = false;
        if (++ticks % 10 == 0) readCommands(client);
        if (ticks % 20 == 0) writeStatus(client);
    }

    private static void readCommands(MinecraftClient client) {
        Path in = DIR.resolve("in.txt");
        if (!Files.exists(in)) return;
        List<String> lines;
        try {
            lines = Files.readAllLines(in, StandardCharsets.UTF_8);
            Files.delete(in);
        } catch (IOException e) {
            return;
        }
        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            try {
                reply(l + " -> " + run(client, l));
            } catch (RuntimeException e) {
                reply(l + " -> ERROR " + e);
            }
        }
    }

    private static String run(MinecraftClient client, String line) {
        int sp = line.indexOf(' ');
        String cmd = sp < 0 ? line : line.substring(0, sp);
        String arg = sp < 0 ? "" : line.substring(sp + 1).trim();
        switch (cmd) {
            case "run" -> {
                if (Plus.mod() == null || client.player == null) return "not in a world";
                Plus.control().start(arg);
                return "started";
            }
            case "stop" -> {
                Plus.control().stop();
                return "stopped";
            }
            case "pause" -> {
                Plus.control().togglePause();
                return Plus.control().state().label;
            }
            case "chat" -> {
                if (client.player == null) return "not in a world";
                client.player.sendChatMessage(arg);
                return "sent";
            }
            case "shot" -> {
                String name = (arg.isEmpty() ? "dev" : arg.replaceAll("[^A-Za-z0-9_-]", "_")) + ".png";
                ScreenshotRecorder.saveScreenshot(client.runDirectory, name, client.getFramebuffer(), t -> { });
                return "screenshots/" + name;
            }
            case "world" -> {
                if (client.world != null) return "already in a world";
                client.startIntegratedServer(arg);
                return "loading " + arg;
            }
            case "quit" -> {
                if (client.world == null) return "no world";
                boolean single = client.isInSingleplayer();
                client.world.disconnect();
                if (single) client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
                else client.disconnect();
                client.setScreen(new TitleScreen());
                return "quit to title";
            }
            case "exit" -> {
                client.scheduleStop();
                return "exiting";
            }
            default -> {
                return "unknown command";
            }
        }
    }

    private static void writeStatus(MinecraftClient client) {
        StringBuilder sb = new StringBuilder();
        sb.append("time ").append(LocalTime.now().format(TIME)).append('\n');
        sb.append("screen ").append(client.currentScreen == null ? "none" : client.currentScreen.getClass().getSimpleName()).append('\n');
        if (client.player != null && Plus.mod() != null) {
            Snapshot s = Plus.snapshot();
            sb.append("state ").append(Plus.control().state().label).append(Plus.control().defending() ? " (defending)" : "").append('\n');
            sb.append("command ").append(Plus.control().lastCommand()).append('\n');
            sb.append("hud ").append(TaskNarrator.goal(Plus.control().lastCommand())).append(" | ")
                    .append(TaskNarrator.action(s.taskChain, "")).append('\n');
            sb.append("chain ").append(String.join(" / ", s.taskChain)).append('\n');
            sb.append(String.format("pos %.1f %.1f %.1f %s%n", s.x, s.y, s.z, s.dimension));
            sb.append("health ").append(s.health).append(" food ").append(s.food)
                    .append(" air ").append(client.player.getAir()).append('\n');
            sb.append("armor ").append(s.armor).append(" offhand ").append(s.offhand).append('\n');
            sb.append("hotbar ").append(s.hotbar).append('\n');
            sb.append("items ");
            for (Map.Entry<String, Integer> e : s.items.entrySet()) sb.append(e.getKey()).append('=').append(e.getValue()).append(' ');
            sb.append('\n');
            sb.append("modules ");
            for (String id : Plus.modules().ids()) {
                var m = Plus.modules().get(id);
                sb.append(id).append('=').append(Plus.modules().isEnabled(id) ? m.status() : "off").append("; ");
            }
            sb.append('\n');
        }
        StringBuilder log = new StringBuilder();
        if (Plus.log() != null) {
            List<LogBuffer.LogLine> lines = Plus.log().snapshot();
            for (LogBuffer.LogLine ll : lines.subList(Math.max(0, lines.size() - 60), lines.size())) {
                log.append('[').append(ll.source()).append("] ").append(ll.message()).append('\n');
            }
        }
        try {
            Files.writeString(DIR.resolve("status.txt"), sb.toString(), StandardCharsets.UTF_8);
            Files.writeString(DIR.resolve("log.txt"), log.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static void reply(String text) {
        try {
            Files.writeString(DIR.resolve("out.txt"), LocalTime.now().format(TIME) + " " + text + "\n",
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }
}
