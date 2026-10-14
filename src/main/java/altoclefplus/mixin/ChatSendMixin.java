package altoclefplus.mixin;

import altoclefplus.Plus;
import altoclefplus.core.Hooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks AltoClef (@) and Baritone (#) chat commands; the panel is the control surface. Priority 500 makes
 * this HEAD callback run before AltoClef's and Baritone's own chat hooks (default priority 1000).
 */
@Mixin(value = ClientPlayerEntity.class, priority = 500)
public abstract class ChatSendMixin {
    private static long lastHintMs;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void altoclefplus$blockBotCommands(String message, CallbackInfo ci) {
        Hooks.hit("chat-block");
        if (!Plus.config().ui.blockChatCommands) return;
        if (!(message.startsWith("@") || message.startsWith("#"))) return;
        ci.cancel();
        Plus.log().add("plus", "Blocked chat command: " + message);
        long now = System.currentTimeMillis();
        if (now - lastHintMs > 10_000) {
            lastHintMs = now;
            MinecraftClient.getInstance().inGameHud.getChatHud()
                    .addMessage(new LiteralText("§7AltoClef Plus: chat commands are off. Use the panel (Right Shift)."));
        }
    }
}
