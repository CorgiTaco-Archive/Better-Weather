package corgitaco.betterweather.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * Safely castable to or extenders of {@link net.minecraft.world.biome.Biome}
 * Used to acquire any new climate data provided by Better Weather for the given biome.
 */
public interface BiomeClimate {

    /**
     * @return temperature modifier for the current weather event and/or season.
     */
    double getTemperatureModifier();

    double getSeasonTemperatureModifier();

    double getWeatherTemperatureModifier(BlockPos pos);


    /**
     * @return temperature modifier for the current weather event and/or season.
     */
    double getHumidityModifier();

    double getSeasonHumidityModifier();

    double getWeatherHumidityModifier(BlockPos pos);

    static BiomeClimate getClimate(Biome biome) {
        return ((BiomeClimate)(Object) biome);
    }
}