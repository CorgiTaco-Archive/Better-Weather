package corgitaco.betterweather.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    private void removeThunderStrength(float delta, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0F); //TODO: Reimplement use during vanilla thunderstorms.
    }
}
