package altoclefplus.mixin;

import altoclefplus.ui.Keybinds;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/** Adds our keys to vanilla options before they are loaded, so they appear in Controls and persist. */
@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Shadow
    @Final
    @Mutable
    public KeyBinding[] allKeys;

    @Inject(method = "load", at = @At("HEAD"))
    private void altoclefplus$addKeys(CallbackInfo ci) {
        KeyBinding[] ours = Keybinds.all();
        for (KeyBinding k : allKeys) {
            if (k == ours[0]) return; // already added (load() can run more than once)
        }
        KeyBinding[] merged = Arrays.copyOf(allKeys, allKeys.length + ours.length);
        System.arraycopy(ours, 0, merged, allKeys.length, ours.length);
        allKeys = merged;
    }
}
