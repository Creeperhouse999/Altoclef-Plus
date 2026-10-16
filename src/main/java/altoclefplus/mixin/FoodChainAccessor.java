package altoclefplus.mixin;

import adris.altoclef.chains.FoodChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets a rest ask AltoClef to eat until full (see CautionPolicy.fillUpWhileResting). */
@Mixin(value = FoodChain.class, remap = false)
public interface FoodChainAccessor {
    @Accessor("_requestFillup")
    void altoclefplus$setRequestFillup(boolean value);
}
