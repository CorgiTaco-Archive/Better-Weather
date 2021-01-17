package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.helper.IsWeatherForced;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorldInfo.class)
public abstract class MixinServerWorldInfo implements IsWeatherForced {
    private boolean weatherIsForced;

    @Override
    public boolean isWeatherForced() {
        return weatherIsForced;
    }

    @Override
    public void setWeatherForced(boolean flag) {
        weatherIsForced = flag;
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    public void neverThundering(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(WeatherData.currentWeatherEvent.getID().equals(WeatherEventSystem.DEFAULT_THUNDER));
    }
}