package corgitaco.betterweather.api.season;

import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public interface Settings {

    double getTemperatureModifier(@Nullable RegistryKey<Biome> biomeKey);

    double getHumidityModifier(@Nullable RegistryKey<Biome> biomeKey);

    double getCropGrowthChanceMultiplier(@Nullable RegistryKey<Biome> biomeKey, Block block);
}
