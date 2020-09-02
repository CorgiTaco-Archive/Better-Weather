package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.class)
public class MixinDimensionSpecialEffects {
    Minecraft minecraft = Minecraft.getInstance();

    @Inject(at = @At("HEAD"), method = "getSunriseColor(FF)[F", cancellable = true)
    private void removeFogColorForBlizzards(float f, float g, CallbackInfoReturnable<@Nullable float[]> cir) {
        if (minecraft.level != null) {
            BetterWeather.BetterWeatherEvents.setWeatherData(minecraft.level);
            if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard() && minecraft.level.getLevelData().isRaining() && minecraft.level.dimensionType() == DimensionType.DEFAULT_OVERWORLD)
                cir.setReturnValue(null);
        }
    }
}
