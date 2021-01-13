package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.access.IsWeatherForced;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorldInfo.class)
public abstract class MixinServerWorldInfo implements IsWeatherForced {
    private boolean weatherIsForced;

    @Override
    public void setWeatherForced(boolean flag) {
        weatherIsForced = flag;
    }

    @Override
    public boolean isWeatherForced() {
        return weatherIsForced;
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    public void neverThundering(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false); //TODO: Reimplement use during vanilla thunderstorms.
    }
}