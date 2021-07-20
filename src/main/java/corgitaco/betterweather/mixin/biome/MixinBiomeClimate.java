package corgitaco.betterweather.mixin.biome;

import corgitaco.betterweather.api.BiomeClimate;
import corgitaco.betterweather.helpers.BiomeModifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(Biome.Climate.class)
public abstract class MixinBiomeClimate implements BiomeModifier, BiomeClimate {

    private Supplier<Float> seasonTempModifier = () -> 0.0F;
    private Supplier<Float> seasonHumidityModifier = () -> 0.0F;

    private Supplier<Float> weatherTempModifier = () -> 0.0F;
    private Supplier<Float> weatherHumidityModifier = () -> 0.0F;

    @Override
    public double getTemperatureModifier() {
        return seasonTempModifier.get() + weatherTempModifier.get();
    }

    @Override
    public double getSeasonTemperatureModifier() {
        return seasonTempModifier.get();
    }

    @Override
    public double getWeatherTemperatureModifier(BlockPos pos) {
        return weatherTempModifier.get();
    }

    @Override
    public double getHumidityModifier() {
        return seasonHumidityModifier.get() + weatherHumidityModifier.get();
    }

    @Override
    public double getSeasonHumidityModifier() {
        return seasonHumidityModifier.get();
    }

    @Override
    public double getWeatherHumidityModifier(BlockPos pos) {
        return weatherHumidityModifier.get();
    }

    @Override
    public void setSeasonTempModifier(float tempModifier) {
        this.seasonTempModifier = () -> tempModifier;
    }

    @Override
    public void setSeasonHumidityModifier(float humidityModifier) {
        this.seasonHumidityModifier = () -> humidityModifier;
    }

    @Override
    public void setWeatherTempModifier(float tempModifier) {
        this.weatherTempModifier = () -> tempModifier;
    }

    @Override
    public void setWeatherHumidityModifier(float humidityModifier) {
        this.weatherHumidityModifier = () -> humidityModifier;
    }
}
