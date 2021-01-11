package corgitaco.betterweather.mixin;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld {

//    @Shadow public float prevRainingStrength;
//
//    @Shadow public float prevThunderingStrength;
//
//    @Shadow public float thunderingStrength;
//
//    @Shadow public float rainingStrength;
//
//    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
//    private void darkSky(float delta, CallbackInfoReturnable<Float> cir) {
//        if (BetterWeather.weatherData != null) {
//            WeatherEvent currentWeatherEvent = WeatherData.currentWeatherEvent;
//            if (currentWeatherEvent.hasSkyDarkness()) {
//                cir.setReturnValue(currentWeatherEvent.skyDarkness(cir.getReturnValue(), delta, this.prevThunderingStrength, this.thunderingStrength, this.prevRainingStrength, this.rainingStrength));
//            }
//        }
//    }
}
