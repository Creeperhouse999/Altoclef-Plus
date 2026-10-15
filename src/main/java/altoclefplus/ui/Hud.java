package altoclefplus.ui;

import altoclefplus.Plus;
import altoclefplus.core.logic.ItemInfo;
import altoclefplus.core.logic.PlusConfig;
import altoclefplus.core.logic.Snapshot;
import altoclefplus.ui.logic.BotState;
import altoclefplus.ui.logic.Milestone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

/** Small top-left overlay: task, state badge, plan preview, alert, key resources. */
public final class Hud {
    private Hud() {}

    public static void render(MatrixStack m, UiModule ui) {
        PlusConfig.Ui cfg = Plus.config().ui;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!cfg.hud || client.options.hudHidden || client.options.debugEnabled || client.player == null) return;
        TextRenderer tr = client.textRenderer;
        Snapshot s = Plus.snapshot();
        int x = 4;
        int y = 4;
        int line = tr.fontHeight + 2;

        if (cfg.hudBadge) {
            BotState st = Plus.control().state();
            boolean defending = Plus.control().defending();
            int color = defending ? 0xFFFF5555 : switch (st) {
                case RUNNING -> 0xFF55FF55;
                case PAUSED -> 0xFFFFFF55;
                case RECOVERING -> 0xFFFFAA00;
                case IDLE -> 0xFFAAAAAA;
            };
            String badge = "[" + (defending ? "Defending" : st.label) + "]";
            DrawableHelper.fill(m, x - 2, y - 2, x + tr.getWidth(badge) + 2, y + tr.fontHeight, 0x90000000);
            tr.drawWithShadow(m, badge, x, y, color);
            y += line;
        }
        if (cfg.hudTask && s.userTaskRunning && !s.taskChain.isEmpty()) {
            String top = shorten(s.taskChain.get(0), 40);
            String leaf = shorten(s.taskChain.get(s.taskChain.size() - 1), 40);
            String text = s.taskChain.size() > 1 ? top + " > " + leaf : top;
            tr.drawWithShadow(m, text, x, y, 0xFFFFFFFF);
            y += line;
        }
        if (cfg.hudPlan && Plus.control().lastCommand().startsWith("gamer")) {
            Milestone cur = ui.plan().current();
            int shown = 0;
            for (Milestone mi : Milestone.values()) {
                if (cur == null || mi.ordinal() < cur.ordinal() || shown >= 3) continue;
                String prefix = mi == cur ? "▶ " : "○ ";
                tr.drawWithShadow(m, prefix + mi.label, x, y, mi == cur ? 0xFFFFFF55 : 0xFFAAAAAA);
                y += line;
                shown++;
            }
        }
        if (cfg.hudRecovery) {
            String alert = ui.activeAlert();
            if (!alert.isEmpty()) {
                int alpha = Math.max(0x10, (int) (ui.alertFade() * 255)) << 24;
                tr.drawWithShadow(m, alert, x, y, alpha | 0xFFAA00);
                y += line;
            }
        }
        if (cfg.hudResources) {
            StringBuilder sb = new StringBuilder();
            for (String id : cfg.hudResourceItems) {
                int n = switch (id) {
                    case "food" -> ItemInfo.totalFoodUnits(s);
                    case "bed" -> ItemInfo.countMatching(s, ItemInfo::isBed);
                    default -> s.count(id);
                };
                if (sb.length() > 0) sb.append("  ");
                sb.append(pretty(id)).append(' ').append(n);
            }
            tr.drawWithShadow(m, sb.toString(), x, y, 0xFFCCCCCC);
        }
    }

    static String shorten(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    static String pretty(String id) {
        return switch (id) {
            case "food" -> "Food";
            case "bed" -> "Beds";
            case "ender_pearl" -> "Pearls";
            case "blaze_rod" -> "Rods";
            case "ender_eye" -> "Eyes";
            default -> id.replace('_', ' ');
        };
    }
}
