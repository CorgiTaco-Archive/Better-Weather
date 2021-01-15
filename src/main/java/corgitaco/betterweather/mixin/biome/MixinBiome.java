package corgitaco.betterweather.mixin.biome;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.season.Season;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Biome.class)
public abstract class MixinBiome {

    @Shadow
    public abstract Biome.Category getCategory();

    @Inject(method = "getDownfall", at = @At("RETURN"), cancellable = true)
    private void modifyDownfall(CallbackInfoReturnable<Float> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE) {
            if (BetterWeather.biomeRegistryEarlyAccess != null) {
                double seasonHumidityModifier = SeasonData.currentSubSeason != null ? Season.getSubSeasonFromEnum(SeasonData.currentSubSeason).getHumidityModifier(BetterWeather.biomeRegistryEarlyAccess.getKey((Biome) (Object) this), false) : 0;
                float modifiedHumidity = (float) (cir.getReturnValue() + seasonHumidityModifier);
                cir.setReturnValue(WeatherData.currentWeatherEvent.modifyHumidity(cir.getReturnValue(), modifiedHumidity, seasonHumidityModifier));
            }
        }
    }

    @Inject(method = "getTemperature()F", at = @At("RETURN"), cancellable = true)
    private void modifyTemperature(CallbackInfoReturnable<Float> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE) {
            if (BetterWeather.biomeRegistryEarlyAccess != null) {
                double seasonTempModifier = SeasonData.currentSubSeason != null ? Season.getSubSeasonFromEnum(SeasonData.currentSubSeason).getTempModifier(BetterWeather.biomeRegistryEarlyAccess.getKey((Biome) (Object) this), false) : 0;
                float modifiedTemp = (float) (cir.getReturnValue() + seasonTempModifier);
                cir.setReturnValue(WeatherData.currentWeatherEvent.modifyTemperature(cir.getReturnValue(), modifiedTemp, seasonTempModifier));
            }
        }
    }
}
