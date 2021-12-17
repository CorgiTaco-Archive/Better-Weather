package corgitaco.betterweather.util;

import com.mojang.serialization.Codec;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.common.season.BWSubseasonSettings;
import corgitaco.betterweather.common.season.storage.OverrideStorage;
import corgitaco.betterweather.util.client.ColorUtil;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.*;

@SuppressWarnings("deprecation")
public class BetterWeatherUtil {

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

    public static int transformFloatColor(Vector3d floatColor) {
        return ColorUtil.pack((int) (floatColor.x() * 255), (int) (floatColor.y() * 255), (int) (floatColor.z() * 255));
    }

    public static Object2DoubleOpenHashMap<Block> transformBlockResourceLocations(Map<ResourceLocation, Double> blockResourceLocationToCropGrowthMultiplierMap) {
        Object2DoubleOpenHashMap<Block> newMap = new Object2DoubleOpenHashMap<>();
        blockResourceLocationToCropGrowthMultiplierMap.forEach((resourceLocation, multiplier) -> {
            if (Registry.BLOCK.keySet().contains(resourceLocation)) {
                newMap.put(Registry.BLOCK.get(resourceLocation), multiplier);
            } else {
                BetterWeather.LOGGER.error("The value: \"" + resourceLocation.toString() + "\" is not a valid block ID...");
            }
        });
        return newMap;
    }

    public static IdentityHashMap<Block, Block> transformBlockBlockResourceLocations(Map<ResourceLocation, ResourceLocation> blockBlockMap) {
        IdentityHashMap<Block, Block> newMap = new IdentityHashMap<>();
        blockBlockMap.forEach((resourceLocation, resourceLocation2) -> {
            if (Registry.BLOCK.keySet().contains(resourceLocation) && Registry.BLOCK.keySet().contains(resourceLocation2)) {
                newMap.put(Registry.BLOCK.get(resourceLocation), Registry.BLOCK.get(resourceLocation2));
            } else {
                BetterWeather.LOGGER.error("The value: \"" + resourceLocation.toString() + "\" is not a valid block ID...");
            }
        });
        return newMap;
    }

    public static TreeMap<ResourceLocation, ResourceLocation> transformBlockBlocksToResourceLocations(Map<Block, Block> blockBlockMap) {
        TreeMap<ResourceLocation, ResourceLocation> newMap = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
        blockBlockMap.forEach((resourceLocation, resourceLocation2) -> {
            newMap.put(Registry.BLOCK.getKey(resourceLocation), Registry.BLOCK.getKey(resourceLocation2));
        });
        return newMap;
    }

    public static IdentityHashMap<RegistryKey<Biome>, OverrideStorage> transformBiomeResourceLocationsToKeys(Map<ResourceLocation, OverrideStorage> blockResourceLocationToCropGrowthMultiplierMap) {
        IdentityHashMap<RegistryKey<Biome>, OverrideStorage> newMap = new IdentityHashMap<>();
        blockResourceLocationToCropGrowthMultiplierMap.forEach((resourceLocation, multiplier) -> {
            newMap.put(RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation), multiplier);
        });
        return newMap;
    }

    public static ReferenceArraySet<RegistryKey<Codec<? extends WeatherEvent>>> transformWeatherLocationsToKeys(Collection<ResourceLocation> blockResourceLocationToCropGrowthMultiplierMap) {
        ReferenceArraySet<RegistryKey<Codec<? extends WeatherEvent>>> newMap = new ReferenceArraySet<>();
        blockResourceLocationToCropGrowthMultiplierMap.forEach((resourceLocation) -> {
            newMap.add(RegistryKey.create(BetterWeatherRegistry.WEATHER_EVENT_KEY, resourceLocation));
        });
        return newMap;
    }
}