package corgitaco.betterweather.util;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.BWSubseasonSettings;
import corgitaco.betterweather.season.storage.OverrideStorage;
import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;

public class BetterWeatherUtil {

    public static final Color DEFAULT_RAIN_SKY = new Color(103, 114, 136);
    public static final Color DEFAULT_RAIN_FOG = new Color(89, 100, 142);
    public static final Color DEFAULT_RAIN_CLOUDS = new Color(158, 158, 158);

    public static final Color DEFAULT_THUNDER_SKY = new Color(42, 45, 51);
    public static final Color DEFAULT_THUNDER_FOG = new Color(85, 95, 135);
    public static final Color DEFAULT_THUNDER_CLOUDS = new Color(37, 37, 37);

    public static int parseHexColor(String targetHexColor) {
        if (!targetHexColor.isEmpty()) {
            try {
                return (int) Long.parseLong(targetHexColor.replace("#", "").replace("0x", ""), 16);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        return Integer.MAX_VALUE;
    }

    public static boolean filterRegistryID(ResourceLocation id, Registry<?> registry, String registryTypeName) {
        if (registry.keySet().contains(id))
            return true;
        else {
            if (!id.toString().contains("modid:dummymob")) {
                BetterWeather.LOGGER.error("\"" + id.toString() + "\" was not a registryID in the " + registryTypeName + "! Skipping entry...");
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static <I, O> O unsafeCast(I obj) {
        return (O) obj;
    }

    public static int transformRainOrThunderTimeToCurrentSeason(int rainOrThunderTime, BWSubseasonSettings previous, BWSubseasonSettings current) {
        double previousMultiplier = previous.getWeatherEventChanceMultiplier();
        double currentMultiplier = current.getWeatherEventChanceMultiplier();
        return transformRainOrThunderTimeToCurrentSeason(rainOrThunderTime, previousMultiplier, currentMultiplier);
    }

    public static int transformRainOrThunderTimeToCurrentSeason(int rainOrThunderTime, double prevMultiplier, double currentMultiplier) {
        double normalTime = rainOrThunderTime * prevMultiplier;
        return (int) (normalTime * 1 / currentMultiplier);
    }


    public static int modifiedColorValue(int original, int target, double blendStrength) {
        return (int) MathHelper.lerp(blendStrength, original, target);
    }

    public static Color blendColor(Color original, Color target, double blendStrength) {
        int modifiedRed = modifiedColorValue(original.getRed(), target.getRed(), blendStrength);
        int modifiedGreen = modifiedColorValue(original.getGreen(), target.getGreen(), blendStrength);
        int modifiedBlue = modifiedColorValue(original.getBlue(), target.getBlue(), blendStrength);
        return new Color(modifiedRed, modifiedGreen, modifiedBlue);
    }

    public static Color transformFloatColor(Vector3d floatColor) {
        return new Color((int) (floatColor.getX() * 255), (int) (floatColor.getY() * 255), (int) (floatColor.getZ() * 255));
    }

    public static IdentityHashMap<Block, Double> transformBlockResourceLocations(Map<ResourceLocation, Double> blockResourceLocationToCropGrowthMultiplierMap) {
        IdentityHashMap<Block, Double> newMap = new IdentityHashMap<>();
        blockResourceLocationToCropGrowthMultiplierMap.forEach((resourceLocation, multiplier) -> {
            if (Registry.BLOCK.keySet().contains(resourceLocation)) {
                newMap.put(Registry.BLOCK.getOrDefault(resourceLocation), multiplier);
            } else {
                BetterWeather.LOGGER.error("The value: \"" + resourceLocation.toString() + "\" is not a valid block ID...");
            }
        });
        return newMap;
    }

    public static IdentityHashMap<RegistryKey<Biome>, OverrideStorage> transformBiomeResourceLocationsToKeys(Map<ResourceLocation, OverrideStorage> blockResourceLocationToCropGrowthMultiplierMap) {
        IdentityHashMap<RegistryKey<Biome>, OverrideStorage> newMap = new IdentityHashMap<>();
        blockResourceLocationToCropGrowthMultiplierMap.forEach((resourceLocation, multiplier) -> {
            newMap.put(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, resourceLocation), multiplier);
        });
        return newMap;
    }
}