package corgitaco.betterweather.api;

import net.minecraft.world.biome.Biome;

/**
 * Safely castable to {@link net.minecraft.world.biome.Biome}
 * Used to acquire the temperature or humidity offset in a Biome provided by Better Weather.
 */
public interface BiomeClimate {

    double getTemperatureModifier();

    double getHumidityModifier();
    
    static BiomeClimate getClimate(Biome biome) {
        return ((BiomeClimate)(Object) biome);
    }
}