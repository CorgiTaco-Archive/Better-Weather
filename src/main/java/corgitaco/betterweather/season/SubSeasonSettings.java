package corgitaco.betterweather.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.util.storage.OverrideStorage;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class SubSeasonSettings {


    public static Codec<SubSeasonSettings> CODEC = RecordCodecBuilder.create((subSeasonSettingsInstance -> {
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
            return subSeasonSettings.client;
        }), Codec.list(Codec.STRING).optionalFieldOf("entityBreedingBlacklist", new ArrayList<>()).forGetter(subSeasonSettings -> {
            return subSeasonSettings.entityTypeBreedingBlacklist.stream().map(Registry.ENTITY_TYPE::getKey).map(ResourceLocation::toString).collect(Collectors.toList());
        })).apply(subSeasonSettingsInstance, SubSeasonSettings::new);
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
    public static final SubSeasonSettings DEFAULT_SPRING_START = new SubSeasonSettings(-0.15, 0.5, 1.5, 1.3, SPRING_START_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5, Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5));
    public static final SubSeasonSettings DEFAULT_SPRING_MID = new SubSeasonSettings(0.1, 0.5, 2.0, 2.0, SPRING_MID_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5));
    public static final SubSeasonSettings DEFAULT_SPRING_END = new SubSeasonSettings(0.25, 0.4, 1.5, 1.7, SPRING_END_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5));
    public static final SubSeasonSettings DEFAULT_SUMMER_START = new SubSeasonSettings(0.35, -0.1, 0.75, 1.15, SUMMER_START_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings());
    public static final SubSeasonSettings DEFAULT_SUMMER_MID = new SubSeasonSettings(0.5, -0.3, 0.2, 1.0, SUMMER_MID_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings());
    public static final SubSeasonSettings DEFAULT_SUMMER_END = new SubSeasonSettings(0.15, -0.1, 0.5, 1.0, SUMMER_END_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings());
    public static final SubSeasonSettings DEFAULT_AUTUMN_START = new SubSeasonSettings(-0.1, 0, 0.7, 0.8, AUTUMN_START_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
    public static final SubSeasonSettings DEFAULT_AUTUMN_MID = new SubSeasonSettings(-0.2, 0, 0.7, 0.75, AUTUMN_MID_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
    public static final SubSeasonSettings DEFAULT_AUTUMN_END = new SubSeasonSettings(-0.3, 0.1, 0.75, 0.65, AUTUMN_END_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
    public static final SubSeasonSettings DEFAULT_WINTER_START = new SubSeasonSettings(-0.4, 0.2, 1.0, 0.6, WINTER_START_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
    public static final SubSeasonSettings DEFAULT_WINTER_MID = new SubSeasonSettings(-0.5, 0.2, 1.0, 0.5, WINTER_MID_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
    public static final SubSeasonSettings DEFAULT_WINTER_END = new SubSeasonSettings(-0.35, 0.2, 1.25, 0.75, WINTER_END_WEATHER_EVENT_CONTROLLER, new SeasonClientSettings(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
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
    private final SeasonClientSettings client;

    //These are not to be serialized by GSON.
    private transient SeasonData.SeasonKey parentSeason;
    private transient IdentityHashMap<Block, Double> cropToMultiplierStorage;
    private transient HashMap<ResourceLocation, OverrideStorage> biomeToOverrideStorage;
    private transient ObjectOpenHashSet<EntityType<?>> entityTypeBreedingBlacklist;

    public SubSeasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, SeasonClientSettings client) {
        this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, client, new ObjectOpenHashSet<>());
    }

    public SubSeasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, SeasonClientSettings client, List<String> entityBreedingBlacklist) {
        this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, client, new HashSet<>(entityBreedingBlacklist));
    }


    public SubSeasonSettings(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, Map<String, Double> weatherEventController, SeasonClientSettings client, Set<String> entityBreedingBlacklist) {
        this.tempModifier = tempModifier;
        this.humidityModifier = humidityModifier;
        this.weatherEventChanceMultiplier = weatherEventChanceMultiplier;
        this.cropGrowthChanceMultiplier = cropGrowthChanceMultiplier;
        this.weatherEventController = new HashMap<>(weatherEventController);
        this.client = client;
        entityTypeBreedingBlacklist = new ObjectOpenHashSet<>(entityBreedingBlacklist.stream().map(ResourceLocation::new).filter((resourceLocation) -> (BetterWeatherUtil.filterRegistryID(resourceLocation, Registry.ENTITY_TYPE, "Entity"))).map(Registry.ENTITY_TYPE::getOptional).map(Optional::get).collect(Collectors.toSet()));
    }

    public SeasonData.SeasonKey getParent() {
        return parentSeason;
    }

    public void setParentSeason(SeasonData.SeasonKey parentSeason) {
        this.parentSeason = parentSeason;
    }

    public IdentityHashMap<Block, Double> getCropToMultiplierStorage() {
        if (cropToMultiplierStorage == null)
            cropToMultiplierStorage = new IdentityHashMap<>();
        return cropToMultiplierStorage;
    }

    public void setCropToMultiplierStorage(IdentityHashMap<Block, Double> cropToMultiplierStorage) {
        this.cropToMultiplierStorage = cropToMultiplierStorage;
    }

    public HashMap<ResourceLocation, OverrideStorage> getBiomeToOverrideStorage() {
        if (biomeToOverrideStorage == null)
            biomeToOverrideStorage = new HashMap<>();
        return biomeToOverrideStorage;
    }

    public void setBiomeToOverrideStorage(HashMap<ResourceLocation, OverrideStorage> biomeToOverrideStorage) {
        this.biomeToOverrideStorage = biomeToOverrideStorage;
    }

    public double getTempModifier(ResourceLocation biome, boolean useSeasonDefault) {
        double defaultValue = tempModifier;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        double tempModifier = this.biomeToOverrideStorage.get(biome).getTempModifier();

        if (tempModifier == Double.MAX_VALUE)
            return defaultValue;
        else
            return tempModifier;
    }

    public double getHumidityModifier(ResourceLocation biome, boolean useSeasonDefault) {
        double defaultValue = humidityModifier;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        double humidityModifier = this.biomeToOverrideStorage.get(biome).getHumidityModifier();

        if (humidityModifier == Double.MAX_VALUE)
            return defaultValue;
        else
            return humidityModifier;
    }

    public double getWeatherEventChanceMultiplier() {
        return weatherEventChanceMultiplier;
    }

    public double getCropGrowthChanceMultiplier(ResourceLocation biome, Block block, boolean useSeasonDefault) {
        if (useSeasonDefault)
            return cropGrowthChanceMultiplier;


        OverrideStorage overrideStorage = this.biomeToOverrideStorage.get(biome);
        if (overrideStorage == null) {
            return getCropToMultiplierStorage().getOrDefault(block, cropGrowthChanceMultiplier);
        }

        double fallBack = overrideStorage.getFallBack();
        if (fallBack == Double.MAX_VALUE)
            fallBack = cropGrowthChanceMultiplier;

        IdentityHashMap<Block, Double> blockToCropGrowthMultiplierMap = overrideStorage.getBlockToCropGrowthMultiplierMap();
        return blockToCropGrowthMultiplierMap.getOrDefault(block, fallBack);
    }

    public HashMap<String, Double> getWeatherEventController() {
        return weatherEventController;
    }

    public SeasonClientSettings getClientSettings() {
        return client;
    }

    public int getTargetFoliageColor(ResourceLocation biome, boolean useSeasonDefault) {
        int defaultValue = client.targetFoliageHexColor;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        int overrideTargetFoliageColor = this.biomeToOverrideStorage.get(biome).getClientStorage().getParsedFoliageHexColor();

        if (overrideTargetFoliageColor == Integer.MAX_VALUE)
            return defaultValue;
        else
            return overrideTargetFoliageColor;
    }

    public double getFoliageColorBlendStrength(ResourceLocation biome, boolean useSeasonDefault) {
        double defaultValue = client.foliageColorBlendStrength;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        double overrideTargetFoliageBlendStrength = this.biomeToOverrideStorage.get(biome).getClientStorage().getFoliageColorBlendStrength();

        if (overrideTargetFoliageBlendStrength == Double.MAX_VALUE)
            return defaultValue;
        else
            return overrideTargetFoliageBlendStrength;
    }

    public int getTargetGrassColor(ResourceLocation biome, boolean useSeasonDefault) {
        int defaultValue = client.targetGrassHexColor;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        int overrideTargetGrassColor = this.biomeToOverrideStorage.get(biome).getClientStorage().getParsedGrassHexColor();

        if (overrideTargetGrassColor == Integer.MAX_VALUE)
            return defaultValue;
        else
            return overrideTargetGrassColor;
    }

    public double getGrassColorBlendStrength(ResourceLocation biome, boolean useSeasonDefault) {
        double defaultValue = client.grassColorBlendStrength;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        double overrideTargetGrassBlendStrength = this.biomeToOverrideStorage.get(biome).getClientStorage().getGrassColorBlendStrength();

        if (overrideTargetGrassBlendStrength == Double.MAX_VALUE)
            return defaultValue;
        else
            return overrideTargetGrassBlendStrength;
    }

    public int getTargetSkyColor(ResourceLocation biome, boolean useSeasonDefault) {
        int defaultValue = client.targetSkyHexColor;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        int overrideTargetSkyColor = this.biomeToOverrideStorage.get(biome).getClientStorage().getParsedSkyHexColor();

        if (overrideTargetSkyColor == Integer.MAX_VALUE)
            return defaultValue;
        else
            return overrideTargetSkyColor;
    }

    public double getSkyColorBlendStrength(ResourceLocation biome, boolean useSeasonDefault) {
        double defaultValue = client.skyColorBlendStrength;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        double overrideTargetGrassBlendStrength = this.biomeToOverrideStorage.get(biome).getClientStorage().getSkyColorBlendStrength();

        if (overrideTargetGrassBlendStrength == Double.MAX_VALUE)
            return defaultValue;
        else
            return overrideTargetGrassBlendStrength;
    }

    public int getTargetFogColor(ResourceLocation biome, boolean useSeasonDefault) {
        int defaultValue = client.targetFogHexColor;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        int overrideTargetFogColor = this.biomeToOverrideStorage.get(biome).getClientStorage().getParsedFogHexColor();

        if (overrideTargetFogColor == Integer.MAX_VALUE)
            return defaultValue;
        else
            return overrideTargetFogColor;
    }

    public double getFogColorBlendStrength(ResourceLocation biome, boolean useSeasonDefault) {
        double defaultValue = client.fogColorBlendStrength;
        if (useSeasonDefault) {
            return defaultValue;
        }

        if (this.getBiomeToOverrideStorage().get(biome) == null) {
            return defaultValue;
        }
        double overrideFogColorBlendStrangth = this.biomeToOverrideStorage.get(biome).getClientStorage().getFogColorBlendStrength();

        if (overrideFogColorBlendStrangth == Double.MAX_VALUE)
            return defaultValue;
        else
            return overrideFogColorBlendStrangth;
    }

    public ObjectOpenHashSet<EntityType<?>> getEntityTypeBreedingBlacklist() {
        if (entityTypeBreedingBlacklist == null)
            entityTypeBreedingBlacklist = new ObjectOpenHashSet<>();
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
