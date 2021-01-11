package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.util.SoundCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Final
    @Shadow
    private GameSettings options;

    @Inject(at = @At("HEAD"), method = "getVolume(Lnet/minecraft/util/SoundCategory;)F", cancellable = true)
    private void weatherVolumeController(SoundCategory category, CallbackInfoReturnable<Float> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.world != null) {
            float modifiedSoundVolume = WeatherData.currentWeatherEvent.modifySoundVolume(category, minecraft, options);
            if (modifiedSoundVolume != Float.MAX_VALUE)
                cir.setReturnValue(modifiedSoundVolume);
        }
    }
}

