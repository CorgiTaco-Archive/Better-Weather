package corgitaco.betterweather.api.season;

import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

/**
 * In the current implementation, each season has 3 (length of {@link Season.Phase}) Subseason Settings.
 */
public interface SubseasonSettings {

    /**
     * @return Temperature modifier for the given biome in the following priority order:
     * <p></p>
     * 1. Biome
     * 2. Subseason
     */
    double getTemperatureModifier(@Nullable RegistryKey<Biome> biomeKey);

    /**
     * @return Temperature modifier for these SubseasonSettings.
     */
    default double getSubseasonTemperatureModifier() {
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
     * @return Temperature modifier for these SubseasonSettings.
     */
    default double getSubseasonHumidityModifier() {
        return getHumidityModifier(null);
    }

    /**
     * @return Crop growth multiplier for the given crop in the following priority order:
     * <p></p>
     * 1. Biome(Crop & Biome is present)
     * <p></p>
     * 2. Biome Default(Crop is not present, Biome is present).
     * <p></p>
     * 3. Subseason(Crop is present, Biome is not present)
     * <p></p>
     * 4. Subseason Default(Neither Crop or Biome is present)
     */
    double getCropGrowthMultiplier(@Nullable RegistryKey<Biome> biomeKey, @Nullable Block crop);

    /**
     * @return Crop growth for these SubseasonSettings.
     */
    default double getSubseasonCropGrowthMultiplier() {
        return getCropGrowthMultiplier(null, null);
    }

    /**
     * @return Crop growth multiplier for the given crop in the following priority order:
     * <p></p>
     * 1. Subseason
     * 2. Subseason Default
     */
    default double getBlockCropGrowthMultiplier(Block crop) {
        return getCropGrowthMultiplier(null, crop);
    }

    /**
     * @return Crop growth multiplier for the given biome in the following priority order:
     * <p></p>
     * 1. Biome default
     * 2. Subseason Default
     */
    default double getBiomeCropGrowthMultiplier(RegistryKey<Biome> biomeKey) {
        return getCropGrowthMultiplier(biomeKey, null);
    }
}
