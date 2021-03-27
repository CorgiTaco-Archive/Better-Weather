package corgitaco.betterweather.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.SubseasonSettings;
import corgitaco.betterweather.season.storage.OverrideStorage;
import corgitaco.betterweather.util.BetterWeatherUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static corgitaco.betterweather.util.BetterWeatherUtil.*;

public class BWSubseasonSettings implements SubseasonSettings {

    public static final Codec<BWSubseasonSettings> CODEC = RecordCodecBuilder.create((subSeasonSettingsInstance -> {
        return subSeasonSettingsInstance.group(Codec.DOUBLE.optionalFieldOf("tempModifier", 0.0).forGetter((subSeasonSettings) -> {
            return subSeasonSettings.tempModifier;
        }), Codec.DOUBLE.optionalFieldOf("humidityModifier", 0.0).forGetter((subSeasonSettings) -> {
            return subSeasonSettings.humidityModifier;
        }), Codec.DOUBLE.optionalFieldOf("weatherEventChanceMultiplier", 0.0).forGetter((subSeasonSettings) -> {
            return subSeasonSettings.weatherEventChanceMultiplier;
        }), Codec.DOUBLE.optionalFieldOf("cropGrowthChanceMultiplier", 0.0).forGetter((subSeasonSettings) -> {
            return subSeasonSettings.cropGrowthChanceMultiplier;
        }), Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("weatherEventController").forGetter((subSeasonSettings) -> {
            return subSeasonSettings.weatherEventController;
        }), SeasonClientSettings.CODEC.fieldOf("client").forGetter(subSeasonSettings -> {
            return subSeasonSettings.clientSettings;
        }), Codec.list(Codec.STRING).optionalFieldOf("entityBreedingBlacklist", new ArrayList<>()).forGetter(subSeasonSettings -> {
            return subSeasonSettings.entityTypeBreedingBlacklist.stream().map(Registry.ENTITY_TYPE::getKey).map(ResourceLocation::toString).collect(Collectors.toList());
        })).apply(subSeasonSettingsInstance, BWSubseasonSettings::new);
    }));

    public static final Codec<BWSubseasonSettings> PACKET_CODEC = RecordCodecBuilder.create((subSeasonSettingsInstance -> {
        return subSeasonSettingsInstance.group(Codec.DOUBLE.optionalFieldOf("tempModifier", 0.0).forGetter((subSeasonSettings) -> {
            return subSeasonSettings.tempModifier;
        }), Codec.DOUBLE.optionalFieldOf("humidityModifier", 0.0).forGetter((subSeasonSettings) -> {
            return subSeasonSettings.humidityModifier;
        }), Codec.DOUBLE.optionalFieldOf("weatherEventChanceMultiplier", 0.0).forGetter((subSeasonSettings) -> {
            return subSeasonSettings.weatherEventChanceMultiplier;
        }), Codec.DOUBLE.optionalFieldOf("cropGrowthChanceMultiplier", 0.0).forGetter((subSeasonSettings) -> {
            return subSeasonSettings.cropGrowthChanceMultiplier;
        }), Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("weatherEventController").forGetter((subSeasonSettings) -> {
            return subSeasonSettings.weatherEventController;
        }), SeasonClientSettings.CODEC.fieldOf("client").forGetter(subSeasonSettings -> {
            return subSeasonSettings.clientSettings;
        }), Codec.list(Codec.STRING).optionalFieldOf("entityBreedingBlacklist", new ArrayList<>()).forGetter(subSeasonSettings -> {
            return subSeasonSettings.entityTypeBreedingBlacklist.stream().map(Registry.ENTITY_TYPE::getKey).map(ResourceLocation::toString).collect(Collectors.toList());
        }), Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).fieldOf("cropToMultiplierStorage").forGetter((subSeasonSettings) -> {
            Map<ResourceLocation, Double> newMap = new IdentityHashMap<>();
            subSeasonSettings.cropToMultiplierStorage.forEach((block, multiplier) -> {
                newMap.put(Registry.BLOCK.getKey(block), multiplier);
            });
            return newMap;
        }), Codec.unboundedMap(ResourceLocation.CODEC, OverrideStorage.PACKET_CODEC).fieldOf("biomeToOverrideStorage").forGetter((subSeasonSettings) -> {
            Map<ResourceLocation, OverrideStorage> newMap = new IdentityHashMap<>();
            subSeasonSettings.biomeToOverrideStorage.forEach((biomeKey, overrideStorage) -> {
                newMap.put(biomeKey.getLocation(), overrideStorage);
            });
            return newMap;
        })).apply(subSeasonSettingsInstance, (tempModifier, humidityModifier, weatherEventMultiplier, cropGrowthChanceMultiplier,
                weatherEventController, clientSettings, entityTypeBreedingBlacklist, cropToMultiplierStorage, biomeToOverrideStorage) ->
                new BWSubseasonSettings(tempModifier, humidityModifier, weatherEventMultiplier, cropGrowthChanceMultiplier, weatherEventController, clientSettings, entityTypeBreedingBlacklist,
                transformBlockResourceLocations(cropToMultiplierStorage), transformBiomeResourceLocationsToKeys(biomeToOverrideStorage)));
    }));

    public static final HashMap<String, Double> SPRING_START_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final HashMap<String, Double> SPRING_MID_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final HashMap<String, Double> SPRING_END_WEATHER_EVENT_CONTROLLER = new HashMap<>();

    public static final HashMap<String, Double> SUMMER_START_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final HashMap<String, Double> SUMMER_MID_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final HashMap<String, Double> SUMMER_END_WEATHER_EVENT_CONTROLLER = new HashMap<>();

    public static final HashMap<String, Double> AUTUMN_START_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final HashMap<String, Double> AUTUMN_MID_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final HashMap<String, Double> AUTUMN_END_WEATHER_EVENT_CONTROLLER = new HashMap<>();

    public static final HashMap<String, Double> WINTER_START_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final HashMap<String, Double> WINTER_MID_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final HashMap<String, Double> WINTER_END_WEATHER_EVENT_CONTROLLER = new HashMap<>();
    public static final BWSubseasonSettings DEFAULT_SPRING_START = new BWSubseasonSettings(-0.15, 0.5, 1.5, 1.3, SPRING_START_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5, Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5));
    public static final BWSubseasonSettings DEFAULT_SPRING_MID = new BWSubseasonSettings(0.1, 0.5, 2.0, 2.0, SPRING_MID_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5));
    public static final BWSubseasonSettings DEFAULT_SPRING_END = new BWSubseasonSettings(0.25, 0.4, 1.5, 1.7, SPRING_END_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5));
    public static final BWSubseasonSettings DEFAULT_SUMMER_START = new BWSubseasonSettings(0.35, -0.1, 0.75, 1.15, SUMMER_START_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings());
    public static final BWSubseasonSettings DEFAULT_SUMMER_MID = new BWSubseasonSettings(0.5, -0.3, 0.2, 1.0, SUMMER_MID_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings());
    public static final BWSubseasonSettings DEFAULT_SUMMER_END = new BWSubseasonSettings(0.15, -0.1, 0.5, 1.0, SUMMER_END_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings());
    public static final BWSubseasonSettings DEFAULT_AUTUMN_START = new BWSubseasonSettings(-0.1, 0, 0.7, 0.8, AUTUMN_START_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
    public static final BWSubseasonSettings DEFAULT_AUTUMN_MID = new BWSubseasonSettings(-0.2, 0, 0.7, 0.75, AUTUMN_MID_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
    public static final BWSubseasonSettings DEFAULT_AUTUMN_END = new BWSubseasonSettings(-0.3, 0.1, 0.75, 0.65, AUTUMN_END_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
    public static final BWSubseasonSettings DEFAULT_WINTER_START = new BWSubseasonSettings(-0.4, 0.2, 1.0, 0.6, WINTER_START_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
    public static final BWSubseasonSettings DEFAULT_WINTER_MID = new BWSubseasonSettings(-0.5, 0.2, 1.0, 0.5, WINTER_MID_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
    public static final BWSubseasonSettings DEFAULT_WINTER_END = new BWSubseasonSettings(-0.35, 0.2, 1.25, 0.75, WINTER_END_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
    public static final IdentityHashMap<Object, OverrideStorage> WINTER_OVERRIDE = Util.make((new IdentityHashMap<>()), (map) -> {
        OverrideStorage overrideStorage = new OverrideStorage();
        overrideStorage.getClientStorage().setTargetFoliageHexColor("#964B00").setTargetGrassHexColor("#964B00"); //Target brown instead of red.
        map.put(Biome.Category.SWAMP, overrideStorage);
    });

    private final double tempModifier;
    private final double humidityModifier;
    private final double weatherEventChanceMultiplier;
    private final double cropGrowthChanceMultiplier; //Final Fallback
    private final HashMap<String, Double> weatherEventController;
    private SeasonClientSettings clientSettings;
    private final IdentityHashMap<Block, Double> cropToMultiplierStorage;
    private final IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage;
    private final ObjectOpenHashSet<EntityType<?>> entityTypeBreedingBlacklist;


    public BWSubseasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, SeasonClientSettings clientSettings) {
        this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, clientSettings, new ObjectOpenHashSet<>(), new IdentityHashMap<>(), new IdentityHashMap<>());
    }

    //Codec constructor
    public BWSubseasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, SeasonClientSettings clientSettings, List<String> entityBreedingBlacklist) {
        this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, clientSettings, new HashSet<>(entityBreedingBlacklist), new IdentityHashMap<>(), new IdentityHashMap<>());
    }

    //Packet Codec Constructor
    public BWSubseasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, SeasonClientSettings clientSettings, List<String> entityBreedingBlacklist, Map<Block, Double> cropToMultiplierStorage, Map<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage) {
        this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, clientSettings, new HashSet<>(entityBreedingBlacklist), new IdentityHashMap<>(cropToMultiplierStorage), new IdentityHashMap<>(biomeToOverrideStorage));
    }

    public BWSubseasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, SeasonClientSettings clientSettings, Set<String> entityBreedingBlacklist, IdentityHashMap<Block, Double> cropToMultiplierStorage, IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage) {
        this.tempModifier = tempModifier;
        this.humidityModifier = humidityModifier;
        this.weatherEventChanceMultiplier = weatherEventChanceMultiplier;
        this.cropGrowthChanceMultiplier = cropGrowthChanceMultiplier;
        this.weatherEventController = new HashMap<>(weatherEventController);
        this.clientSettings = clientSettings;
        this.entityTypeBreedingBlacklist = new ObjectOpenHashSet<>(entityBreedingBlacklist.stream().map(ResourceLocation::new).filter((resourceLocation) -> (BetterWeatherUtil.filterRegistryID(resourceLocation, Registry.ENTITY_TYPE, "Entity"))).map(Registry.ENTITY_TYPE::getOptional).map(Optional::get).collect(Collectors.toSet()));
        this.cropToMultiplierStorage = cropToMultiplierStorage;
        this.biomeToOverrideStorage = biomeToOverrideStorage;
    }

    public void setClient(SeasonClientSettings clientSettings) {
        this.clientSettings = clientSettings;
    }

    public IdentityHashMap<Block, Double> getCropToMultiplierStorage() {
        return cropToMultiplierStorage;
    }

    public void setCropToMultiplierStorage(IdentityHashMap<Block, Double> cropToMultiplierStorage) {
        this.cropToMultiplierStorage.putAll(cropToMultiplierStorage);
    }

    public IdentityHashMap<RegistryKey<Biome>, OverrideStorage> getBiomeToOverrideStorage() {
        return biomeToOverrideStorage;
    }

    public void setBiomeToOverrideStorage(IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage) {
        this.biomeToOverrideStorage.putAll(biomeToOverrideStorage);
    }

    @Override
    public double getTemperatureModifier(RegistryKey<Biome> biomeKey) {
        double defaultValue = tempModifier;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double tempModifier = this.getBiomeToOverrideStorage().get(biomeKey).getTempModifier();
        return tempModifier == Double.MAX_VALUE ? defaultValue : tempModifier;
    }

    @Override
    public double getHumidityModifier(RegistryKey<Biome> biomeKey) {
        double defaultValue = humidityModifier;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double humidityModifier = this.getBiomeToOverrideStorage().get(biomeKey).getHumidityModifier();
        return humidityModifier == Double.MAX_VALUE ? defaultValue : humidityModifier;
    }

    public double getWeatherEventChanceMultiplier() {
        return weatherEventChanceMultiplier;
    }

    @Override
    public double getCropGrowthMultiplier(@Nullable RegistryKey<Biome> biomeKey, @Nullable Block block) {
        if (!getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return getCropToMultiplierStorage().getOrDefault(block, cropGrowthChanceMultiplier);
        }

        OverrideStorage overrideStorage = getBiomeToOverrideStorage().get(biomeKey);
        double fallBack = overrideStorage.getFallBack();

        return overrideStorage.getBlockToCropGrowthMultiplierMap().getOrDefault(block, fallBack == Double.MAX_VALUE ? cropGrowthChanceMultiplier : fallBack);
    }

    public HashMap<String, Double> getWeatherEventController() {
        return weatherEventController;
    }

    public SeasonClientSettings getClientSettings() {
        return clientSettings;
    }

    public int getTargetFoliageColor(RegistryKey<Biome> biomeKey) {
        int defaultValue = clientSettings.targetFoliageHexColor;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        int overrideTargetFoliageColor = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getParsedFoliageHexColor();
        return overrideTargetFoliageColor == Integer.MAX_VALUE ? defaultValue : overrideTargetFoliageColor;
    }

    public double getFoliageColorBlendStrength(RegistryKey<Biome> biomeKey) {
        double defaultValue = clientSettings.foliageColorBlendStrength;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double overrideTargetFoliageBlendStrength = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getFoliageColorBlendStrength();
        return overrideTargetFoliageBlendStrength == Double.MAX_VALUE ? defaultValue : overrideTargetFoliageBlendStrength;
    }

    public int getTargetGrassColor(RegistryKey<Biome> biomeKey) {
        int defaultValue = clientSettings.targetGrassHexColor;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        int overrideTargetGrassColor = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getParsedGrassHexColor();
        return overrideTargetGrassColor == Integer.MAX_VALUE ? defaultValue : overrideTargetGrassColor;
    }

    public double getGrassColorBlendStrength(RegistryKey<Biome> biomeKey) {
        double defaultValue = clientSettings.grassColorBlendStrength;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double overrideTargetGrassBlendStrength = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getGrassColorBlendStrength();
        return overrideTargetGrassBlendStrength == Double.MAX_VALUE ? defaultValue : overrideTargetGrassBlendStrength;
    }

    public int getTargetSkyColor(RegistryKey<Biome> biomeKey) {
        int defaultValue = clientSettings.targetSkyHexColor;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        int overrideTargetSkyColor = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getParsedSkyHexColor();
        return overrideTargetSkyColor == Integer.MAX_VALUE ? defaultValue : overrideTargetSkyColor;
    }

    public double getSkyColorBlendStrength(RegistryKey<Biome> biomeKey) {
        double defaultValue = clientSettings.skyColorBlendStrength;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double overrideTargetGrassBlendStrength = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getSkyColorBlendStrength();
        return overrideTargetGrassBlendStrength == Double.MAX_VALUE ? defaultValue : overrideTargetGrassBlendStrength;
    }

    public int getTargetFogColor(RegistryKey<Biome> biomeKey) {
        int defaultValue = clientSettings.targetFogHexColor;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        int overrideTargetFogColor = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getParsedFogHexColor();
        return overrideTargetFogColor == Integer.MAX_VALUE ? defaultValue : overrideTargetFogColor;
    }

    public double getFogColorBlendStrength(RegistryKey<Biome> biomeKey) {
        double defaultValue = clientSettings.fogColorBlendStrength;
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double overrideFogColorBlendStrangth = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getFogColorBlendStrength();
        return overrideFogColorBlendStrangth == Double.MAX_VALUE ? defaultValue : overrideFogColorBlendStrangth;
    }

    public ObjectOpenHashSet<EntityType<?>> getEntityTypeBreedingBlacklist() {
        return entityTypeBreedingBlacklist;
    }

    public static class SeasonClientSettings {

        public static final Codec<SeasonClientSettings> CODEC = RecordCodecBuilder.create(seasonClientSettingsInstance -> {
            return seasonClientSettingsInstance.group(Codec.STRING.optionalFieldOf("targetFoliageHexColor", "").forGetter((seasonClientSettings) -> {
                return seasonClientSettings.targetFoliageHexColor == Integer.MIN_VALUE ? "" : Integer.toHexString(seasonClientSettings.targetFoliageHexColor);
            }), Codec.DOUBLE.fieldOf("foliageColorBlendStrength").orElse(0.0).forGetter((seasonClientSettings) -> {
                return seasonClientSettings.foliageColorBlendStrength;
            }), Codec.STRING.fieldOf("targetGrassHexColor").orElse("").forGetter((seasonClientSettings) -> {
                return seasonClientSettings.targetGrassHexColor == Integer.MIN_VALUE ? "" : Integer.toHexString(seasonClientSettings.targetGrassHexColor);
            }), Codec.DOUBLE.fieldOf("grassColorBlendStrength").orElse(0.0).forGetter((seasonClientSettings) -> {
                return seasonClientSettings.foliageColorBlendStrength;
            }), Codec.STRING.fieldOf("targetSkyHexColor").orElse("").forGetter((seasonClientSettings) -> {
                return seasonClientSettings.targetSkyHexColor == Integer.MIN_VALUE ? "" : Integer.toHexString(seasonClientSettings.targetSkyHexColor);
            }), Codec.DOUBLE.fieldOf("skyColorBlendStrength").orElse(0.0).forGetter((seasonClientSettings) -> {
                return seasonClientSettings.skyColorBlendStrength;
            }), Codec.STRING.fieldOf("targetFogHexColor").orElse("").forGetter((seasonClientSettings) -> {
                return seasonClientSettings.targetFogHexColor == Integer.MIN_VALUE ? "" : Integer.toHexString(seasonClientSettings.targetFogHexColor);
            }), Codec.DOUBLE.fieldOf("fogColorBlendStrength").orElse(0.0).forGetter((seasonClientSettings) -> {
                return seasonClientSettings.fogColorBlendStrength;
            })).apply(seasonClientSettingsInstance, SeasonClientSettings::new);
        });

        private final int targetFoliageHexColor;
        private final double foliageColorBlendStrength;
        private final int targetGrassHexColor;
        private final double grassColorBlendStrength;
        private final int targetSkyHexColor;
        private final double skyColorBlendStrength;
        private final int targetFogHexColor;
        private final double fogColorBlendStrength;

        public SeasonClientSettings() {
            this("", 0, "", 0);
        }

        public SeasonClientSettings(String targetFoliageHexColor, double foliageColorBlendStrength, String targetGrassColor, double grassColorBlendStrength) {
            this(targetFoliageHexColor, foliageColorBlendStrength, targetGrassColor, grassColorBlendStrength, targetGrassColor, 0, targetGrassColor, 0);
        }

        public SeasonClientSettings(String targetFoliageHexColor, double foliageColorBlendStrength, String targetGrassColor, double grassColorBlendStrength, String targetSkyHexColor, double skyColorBlendStrength, String targetFogHexColor, double fogColorBlendStrength) {
            this(tryParseColor(targetFoliageHexColor), foliageColorBlendStrength, tryParseColor(targetGrassColor), grassColorBlendStrength, tryParseColor(targetSkyHexColor), skyColorBlendStrength, tryParseColor(targetFogHexColor), fogColorBlendStrength);
        }

        public SeasonClientSettings(int targetFoliageHexColor, double foliageColorBlendStrength, int targetGrassColor, double grassColorBlendStrength, int targetSkyHexColor, double skyColorBlendStrength, int targetFogHexColor, double fogColorBlendStrength) {
            this.targetFoliageHexColor = targetFoliageHexColor;
            this.foliageColorBlendStrength = foliageColorBlendStrength;
            this.targetGrassHexColor = targetGrassColor;
            this.grassColorBlendStrength = grassColorBlendStrength;
            this.targetSkyHexColor = targetSkyHexColor;
            this.targetFogHexColor = targetFogHexColor;
            this.fogColorBlendStrength = fogColorBlendStrength;
            this.skyColorBlendStrength = skyColorBlendStrength;
        }


        public static int tryParseColor(String input) {
            int result = Integer.MIN_VALUE;

            if (input.isEmpty()) {
                return result;
            }

            try {
                result = (int) Long.parseLong(input.replace("#", "").replace("0x", ""), 16);
            } catch (NumberFormatException e) {
                BetterWeather.LOGGER.info(e.toString());
            }
            return result;
        }
    }
}
