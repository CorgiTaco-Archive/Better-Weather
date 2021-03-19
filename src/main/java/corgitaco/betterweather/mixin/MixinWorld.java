package corgitaco.betterweather.mixin;

import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.BetterWeatherWorldData;
import corgitaco.betterweather.datastorage.BetterWeatherEventData;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld implements BetterWeatherWorldData {

    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    private void removeThunderStrength(float delta, CallbackInfoReturnable<Float> cir) {
        if (this.getSeasonContext() != null)
            cir.setReturnValue(0.0F);
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void markIsThunderingIfThunderingEvent(CallbackInfoReturnable<Boolean> cir) {
        if (this.getSeasonContext() != null) {
            cir.setReturnValue(BetterWeatherEventData.get((World) (Object) this).getEventID().equals(WeatherEventSystem.DEFAULT_THUNDER));
        }
    }
}
