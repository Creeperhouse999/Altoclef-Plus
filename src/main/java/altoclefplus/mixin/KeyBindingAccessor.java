package altoclefplus.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("boundKey")
    net.minecraft.client.util.InputUtil.Key altoclefplus$getBoundKey();

    @Accessor("CATEGORY_ORDER_MAP")
    static Map<String, Integer> altoclefplus$getCategoryOrderMap() {
        throw new AssertionError();
    }
}
