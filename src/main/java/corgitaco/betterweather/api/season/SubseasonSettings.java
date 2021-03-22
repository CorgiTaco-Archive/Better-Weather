package corgitaco.betterweather.api.season;

import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public interface SubseasonSettings {

    /**
     * @return Temperature modifier for the given biome in the following priority order:
     * <p></p>
     * 1. Biome
     * 2. Subseason
     */
    double getTemperatureModifier(@Nullable RegistryKey<Biome> biomeKey);

    /**
     * @return Temperature modifier for these SubSeasonSettings.
     */
    default double getSubSeasonTemperatureModifier() {
        return getTemperatureModifier(null);
    }

    /**
     * @return Humidity modifier for the given biome in the following priority order:
     * <p></p>
     * 1. Biome
     * 2. Subseason
     */
    double getHumidityModifier(@Nullable RegistryKey<Biome> biomeKey);

    /**
     * @return Temperature modifier for these SubSeasonSettings.
     */
    default double getSubSeasonHumidityModifier() {
        return getHumidityModifier(null);
    }

    /**
     * @return Crop growth multiplier for the given crop in the following priority order:
     * <p></p>
     * 1. Biome(Crop & Biome is present)
     * <p></p>
     * 2. Biome Default(Crop is not present, Biome is present).
     * <p></p>
     * 3. Sub-Season(Crop is present, Biome is not present)
     * <p></p>
     * 4. Sub-Season Default(Neither Crop or Biome is present)
     */
    double getCropGrowthMultiplier(@Nullable RegistryKey<Biome> biomeKey, @Nullable Block crop);

    /**
     * @return Crop growth for these SubSeasonSettings.
     */
    default double getSubSeasonCropGrowthMultiplier() {
        return getCropGrowthMultiplier(null, null);
    }

    /**
     * @return Crop growth multiplier for the given crop in the following priority order:
     * <p></p>
     * 1. Sub-Season
     * 2. Sub-Season Default
     */
    default double getBlockCropGrowthMultiplier(Block crop) {
        return getCropGrowthMultiplier(null, crop);
    }

    /**
     * @return Crop growth multiplier for the given biome in the following priority order:
     * <p></p>
     * 1. Biome default
     * 2. Sub-Season Default
     */
    default double getBiomeCropGrowthMultiplier(RegistryKey<Biome> biomeKey) {
        return getCropGrowthMultiplier(biomeKey, null);
    }
}
