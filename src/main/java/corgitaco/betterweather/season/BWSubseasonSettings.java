package corgitaco.betterweather.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.season.SubseasonSettings;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.season.storage.OverrideStorage;
import corgitaco.betterweather.util.BetterWeatherUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.ITag;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static corgitaco.betterweather.util.BetterWeatherUtil.transformBiomeResourceLocationsToKeys;
import static corgitaco.betterweather.util.BetterWeatherUtil.transformBlockResourceLocations;
import static corgitaco.betterweather.util.client.ColorUtil.pack;

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
        }), ColorSettings.CODEC.fieldOf("client").forGetter(subSeasonSettings -> {
            return subSeasonSettings.clientSettings;
        }), Codec.list(Codec.STRING).optionalFieldOf("entityBreedingBlacklist", new ArrayList<>()).forGetter(subSeasonSettings -> {
            return !subSeasonSettings.entityTypeBreedingBlacklist.isEmpty() ? subSeasonSettings.entityTypeBreedingBlacklist.stream().map(Registry.ENTITY_TYPE::getKey).map(ResourceLocation::toString).collect(Collectors.toList()) : Arrays.asList(new ResourceLocation("modid", "dummymob").toString(), new ResourceLocation("modid", "dummymob2").toString());
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
        }), ColorSettings.CODEC.fieldOf("client").forGetter(subSeasonSettings -> {
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

    public static final String RAIN = "RAIN";
    public static final String THUNDER = "THUNDER";

    public static final HashMap<String, Double> SPRING_START_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final HashMap<String, Double> SPRING_MID_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final HashMap<String, Double> SPRING_END_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });

    public static final HashMap<String, Double> SUMMER_START_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final HashMap<String, Double> SUMMER_MID_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final HashMap<String, Double> SUMMER_END_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });

    public static final HashMap<String, Double> AUTUMN_START_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final HashMap<String, Double> AUTUMN_MID_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final HashMap<String, Double> AUTUMN_END_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });

    public static final HashMap<String, Double> WINTER_START_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final HashMap<String, Double> WINTER_MID_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final HashMap<String, Double> WINTER_END_WEATHER_EVENT_CONTROLLER = Util.make(new HashMap<>(), (map) -> {
        map.put(RAIN, 1.0);
        map.put(THUNDER, 0.75);
    });
    public static final BWSubseasonSettings DEFAULT_SPRING_START = new BWSubseasonSettings(-0.15, 0.5, 1.5, 1.3, SPRING_START_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(51, 97, 50), 0.5, pack(51, 97, 50), 0.5));
    public static final BWSubseasonSettings DEFAULT_SPRING_MID = new BWSubseasonSettings(0.1, 0.5, 2.0, 2.0, SPRING_MID_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(41, 87, 2), 0.5, pack(41, 87, 2), 0.5));
    public static final BWSubseasonSettings DEFAULT_SPRING_END = new BWSubseasonSettings(0.25, 0.4, 1.5, 1.7, SPRING_END_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(20, 87, 2), 0.5, pack(20, 87, 2), 0.5));
    public static final BWSubseasonSettings DEFAULT_SUMMER_START = new BWSubseasonSettings(0.35, -0.1, 0.75, 1.15, SUMMER_START_WEATHER_EVENT_CONTROLLER, new ColorSettings());
    public static final BWSubseasonSettings DEFAULT_SUMMER_MID = new BWSubseasonSettings(0.5, -0.3, 0.2, 1.0, SUMMER_MID_WEATHER_EVENT_CONTROLLER, new ColorSettings());
    public static final BWSubseasonSettings DEFAULT_SUMMER_END = new BWSubseasonSettings(0.15, -0.1, 0.5, 1.0, SUMMER_END_WEATHER_EVENT_CONTROLLER, new ColorSettings());
    public static final BWSubseasonSettings DEFAULT_AUTUMN_START = new BWSubseasonSettings(-0.1, 0, 0.7, 0.8, AUTUMN_START_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(155, 103, 60), 0.5, pack(155, 103, 60), 0.5));
    public static final BWSubseasonSettings DEFAULT_AUTUMN_MID = new BWSubseasonSettings(-0.2, 0, 0.7, 0.75, AUTUMN_MID_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(155, 103, 60), 0.5, pack(155, 103, 60), 0.5));
    public static final BWSubseasonSettings DEFAULT_AUTUMN_END = new BWSubseasonSettings(-0.3, 0.1, 0.75, 0.65, AUTUMN_END_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(155, 103, 60), 0.5, pack(155, 103, 60), 0.5));
    public static final BWSubseasonSettings DEFAULT_WINTER_START = new BWSubseasonSettings(-0.4, 0.2, 1.0, 0.6, WINTER_START_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(165, 42, 42), 0.5, pack(165, 42, 42), 0.5));
    public static final BWSubseasonSettings DEFAULT_WINTER_MID = new BWSubseasonSettings(-0.5, 0.2, 1.0, 0.5, WINTER_MID_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(165, 42, 42), 0.5, pack(165, 42, 42), 0.5));
    public static final BWSubseasonSettings DEFAULT_WINTER_END = new BWSubseasonSettings(-0.35, 0.2, 1.25, 0.75, WINTER_END_WEATHER_EVENT_CONTROLLER, new ColorSettings(pack(165, 42, 42), 0.5, pack(165, 42, 42), 0.5));
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
    private final IdentityHashMap<Block, Double> cropToMultiplierStorage;
    private final IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage;
    private final ObjectOpenHashSet<EntityType<?>> entityTypeBreedingBlacklist;
    private ColorSettings clientSettings;
    private ITag.INamedTag<Block> enhancedCrops;
    private ITag.INamedTag<Block> unenhancedCrops;

    public BWSubseasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, ColorSettings clientSettings) {
        this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, clientSettings, new ObjectOpenHashSet<>(), new IdentityHashMap<>(), new IdentityHashMap<>());
    }

    //Codec constructor
    public BWSubseasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, ColorSettings clientSettings, List<String> entityBreedingBlacklist) {
        this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, clientSettings, new HashSet<>(entityBreedingBlacklist), new IdentityHashMap<>(), new IdentityHashMap<>());
    }

    //Packet Codec Constructor
    public BWSubseasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, ColorSettings clientSettings, List<String> entityBreedingBlacklist, Map<Block, Double> cropToMultiplierStorage, Map<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage) {
        this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, clientSettings, new HashSet<>(entityBreedingBlacklist), new IdentityHashMap<>(cropToMultiplierStorage), new IdentityHashMap<>(biomeToOverrideStorage));
    }

    public BWSubseasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, ColorSettings clientSettings, Set<String> entityBreedingBlacklist, IdentityHashMap<Block, Double> cropToMultiplierStorage, IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage) {
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

    public void setClient(ColorSettings clientSettings) {
        this.clientSettings = clientSettings;
    }

    public void setCropTags(ITag.INamedTag<Block> enhancedCrops, ITag.INamedTag<Block> unenhancedCrops) {
        this.enhancedCrops = enhancedCrops;
        this.unenhancedCrops = unenhancedCrops;
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

    public ColorSettings getClientSettings() {
        return clientSettings;
    }

    public int getTargetFoliageColor(RegistryKey<Biome> biomeKey) {
        int defaultValue = clientSettings.getTargetFoliageHexColor();
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        int overrideTargetFoliageColor = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getParsedFoliageHexColor();
        return overrideTargetFoliageColor == Integer.MAX_VALUE ? defaultValue : overrideTargetFoliageColor;
    }

    public double getFoliageColorBlendStrength(RegistryKey<Biome> biomeKey) {
        double defaultValue = clientSettings.getFoliageColorBlendStrength();
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double overrideTargetFoliageBlendStrength = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getFoliageColorBlendStrength();
        return overrideTargetFoliageBlendStrength == Double.MAX_VALUE ? defaultValue : overrideTargetFoliageBlendStrength;
    }

    public int getTargetGrassColor(RegistryKey<Biome> biomeKey) {
        int defaultValue = clientSettings.getTargetGrassHexColor();
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        int overrideTargetGrassColor = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getParsedGrassHexColor();
        return overrideTargetGrassColor == Integer.MAX_VALUE ? defaultValue : overrideTargetGrassColor;
    }

    public double getGrassColorBlendStrength(RegistryKey<Biome> biomeKey) {
        double defaultValue = clientSettings.getGrassColorBlendStrength();
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double overrideTargetGrassBlendStrength = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getGrassColorBlendStrength();
        return overrideTargetGrassBlendStrength == Double.MAX_VALUE ? defaultValue : overrideTargetGrassBlendStrength;
    }

    public int getTargetSkyColor(RegistryKey<Biome> biomeKey) {
        int defaultValue = clientSettings.getTargetSkyHexColor();
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        int overrideTargetSkyColor = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getParsedSkyHexColor();
        return overrideTargetSkyColor == Integer.MAX_VALUE ? defaultValue : overrideTargetSkyColor;
    }

    public double getSkyColorBlendStrength(RegistryKey<Biome> biomeKey) {
        double defaultValue = clientSettings.getSkyColorBlendStrength();
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double overrideTargetGrassBlendStrength = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getSkyColorBlendStrength();
        return overrideTargetGrassBlendStrength == Double.MAX_VALUE ? defaultValue : overrideTargetGrassBlendStrength;
    }

    public int getTargetFogColor(RegistryKey<Biome> biomeKey) {
        int defaultValue = clientSettings.getTargetFogHexColor();
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        int overrideTargetFogColor = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getParsedFogHexColor();
        return overrideTargetFogColor == Integer.MAX_VALUE ? defaultValue : overrideTargetFogColor;
    }

    public double getFogColorBlendStrength(RegistryKey<Biome> biomeKey) {
        double defaultValue = clientSettings.getFogColorBlendStrength();
        if (!this.getBiomeToOverrideStorage().containsKey(biomeKey)) {
            return defaultValue;
        }

        double overrideFogColorBlendStrangth = this.getBiomeToOverrideStorage().get(biomeKey).getClientStorage().getFogColorBlendStrength();
        return overrideFogColorBlendStrangth == Double.MAX_VALUE ? defaultValue : overrideFogColorBlendStrangth;
    }

    public ObjectOpenHashSet<EntityType<?>> getEntityTypeBreedingBlacklist() {
        return entityTypeBreedingBlacklist;
    }

    public ITag.INamedTag<Block> getEnhancedCrops() {
        return enhancedCrops;
    }

    public ITag.INamedTag<Block> getUnenhancedCrops() {
        return unenhancedCrops;
    }
}
