package corgitaco.betterweather.season;

import com.google.common.collect.Sets;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.BetterWeatherEntryPoint;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.util.storage.OverrideStorage;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

public class Season {

    public static final Season SPRING = new Season(SubSeason.SPRING_START, SubSeason.SPRING_MID, SubSeason.SPRING_END);
    public static final Season SUMMER = new Season(SubSeason.SUMMER_START, SubSeason.SUMMER_MID, SubSeason.SUMMER_END);
    public static final Season AUTUMN = new Season(SubSeason.AUTUMN_START, SubSeason.AUTUMN_MID, SubSeason.AUTUMN_END);
    public static final Season WINTER = new Season(SubSeason.WINTER_START, SubSeason.WINTER_MID, SubSeason.WINTER_END);


    public static Map<String, Season> SEASON_MAP = Util.make((new TreeMap<>()), (map) -> {
        map.put(SeasonData.SeasonVal.SPRING.toString(), SPRING);
        map.put(SeasonData.SeasonVal.SUMMER.toString(), SUMMER);
        map.put(SeasonData.SeasonVal.AUTUMN.toString(), AUTUMN);
        map.put(SeasonData.SeasonVal.WINTER.toString(), WINTER);
    });

    public static Map<String, SubSeason> SUB_SEASON_MAP = Util.make((new TreeMap<>()), (map) -> {
        map.put(SeasonData.SubSeasonVal.SPRING_START.toString(), SEASON_MAP.get(SeasonData.SeasonVal.SPRING.toString()).getStart());
        map.put(SeasonData.SubSeasonVal.SPRING_MID.toString(), SEASON_MAP.get(SeasonData.SeasonVal.SPRING.toString()).getMid());
        map.put(SeasonData.SubSeasonVal.SPRING_END.toString(), SEASON_MAP.get(SeasonData.SeasonVal.SPRING.toString()).getEnd());
        map.put(SeasonData.SubSeasonVal.SUMMER_START.toString(), SEASON_MAP.get(SeasonData.SeasonVal.SUMMER.toString()).getStart());
        map.put(SeasonData.SubSeasonVal.SUMMER_MID.toString(), SEASON_MAP.get(SeasonData.SeasonVal.SUMMER.toString()).getMid());
        map.put(SeasonData.SubSeasonVal.SUMMER_END.toString(), SEASON_MAP.get(SeasonData.SeasonVal.SUMMER.toString()).getEnd());
        map.put(SeasonData.SubSeasonVal.AUTUMN_START.toString(), SEASON_MAP.get(SeasonData.SeasonVal.AUTUMN.toString()).getStart());
        map.put(SeasonData.SubSeasonVal.AUTUMN_MID.toString(), SEASON_MAP.get(SeasonData.SeasonVal.AUTUMN.toString()).getMid());
        map.put(SeasonData.SubSeasonVal.AUTUMN_END.toString(), SEASON_MAP.get(SeasonData.SeasonVal.AUTUMN.toString()).getEnd());
        map.put(SeasonData.SubSeasonVal.WINTER_START.toString(), SEASON_MAP.get(SeasonData.SeasonVal.WINTER.toString()).getStart());
        map.put(SeasonData.SubSeasonVal.WINTER_MID.toString(), SEASON_MAP.get(SeasonData.SeasonVal.WINTER.toString()).getMid());
        map.put(SeasonData.SubSeasonVal.WINTER_END.toString(), SEASON_MAP.get(SeasonData.SeasonVal.WINTER.toString()).getEnd());
    });


    public static Season getSeasonFromEnum(SeasonData.SeasonVal season) {
        return SEASON_MAP.get(season.toString());
    }

    public static SubSeason getSubSeasonFromEnum(SeasonData.SubSeasonVal season) {
        return SUB_SEASON_MAP.get(season.toString());
    }


    private final SubSeason start;
    private final SubSeason mid;
    private final SubSeason end;

    private final transient Set<SubSeason> subSeasons;

    public Season(SubSeason start, SubSeason mid, SubSeason end) {
        this.start = start;
        this.mid = mid;
        this.end = end;
        subSeasons = Sets.newHashSet(start, mid, end);
    }

    public SubSeason getStart() {
        return start;
    }

    public SubSeason getMid() {
        return mid;
    }

    public SubSeason getEnd() {
        return end;
    }

    public Set<SubSeason> getSubSeasons() {
        return subSeasons;
    }

    public boolean containsSubSeason(SeasonData.SubSeasonVal subSeason) {
        return subSeason == start.getSubSeasonVal() || subSeason == mid.getSubSeasonVal() || subSeason == end.getSubSeasonVal();
    }

    public static class SubSeason {

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

        static {
            for (WeatherEvent weatherEvent : BetterWeatherEntryPoint.WEATHER_EVENTS) {
                String name = weatherEvent.getID().toString();
                if (!name.equals(WeatherEventSystem.CLEAR.toString())) {
                    SPRING_START_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getSpringStartWeight());
                    SPRING_MID_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getSpringMidWeight());
                    SPRING_END_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getSpringEndWeight());

                    SUMMER_START_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getSummerStartWeight());
                    SUMMER_MID_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getSummerMidWeight());
                    SUMMER_END_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getSummerEndWeight());

                    AUTUMN_START_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getAutumnStartWeight());
                    AUTUMN_MID_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getAutumnMidWeight());
                    AUTUMN_END_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getAutumnEndWeight());

                    WINTER_START_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getWinterStartWeight());
                    WINTER_MID_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getWinterMidWeight());
                    WINTER_END_WEATHER_EVENT_CONTROLLER.put(name, weatherEvent.getSeasonChance().getWinterEndWeight());
                }
            }
        }


        public static final SubSeason SPRING_START = new SubSeason(-0.15, 0.5, 1.5, 1.3, SPRING_START_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5, Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5));
        public static final SubSeason SPRING_MID = new SubSeason(0.1, 0.5, 2.0, 2.0, SPRING_MID_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5));
        public static final SubSeason SPRING_END = new SubSeason(0.25, 0.4, 1.5, 1.7, SPRING_END_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5));

        public static final SubSeason SUMMER_START = new SubSeason(0.35, -0.1, 0.75, 1.15, SUMMER_START_WEATHER_EVENT_CONTROLLER, new SeasonClient());
        public static final SubSeason SUMMER_MID = new SubSeason(0.5, -0.3, 0.2, 1.0, SUMMER_MID_WEATHER_EVENT_CONTROLLER, new SeasonClient());
        public static final SubSeason SUMMER_END = new SubSeason(0.15, -0.1, 0.5, 1.0, SUMMER_END_WEATHER_EVENT_CONTROLLER, new SeasonClient());

        public static final SubSeason AUTUMN_START = new SubSeason(-0.1, 0, 0.7, 0.8, AUTUMN_START_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
        public static final SubSeason AUTUMN_MID = new SubSeason(-0.2, 0, 0.7, 0.75, AUTUMN_MID_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
        public static final SubSeason AUTUMN_END = new SubSeason(-0.3, 0.1, 0.75, 0.65, AUTUMN_END_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));

        public static final SubSeason WINTER_START = new SubSeason(-0.4, 0.2, 1.0, 0.6, WINTER_START_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
        public static final SubSeason WINTER_MID = new SubSeason(-0.5, 0.2, 1.0, 0.5, WINTER_MID_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
        public static final SubSeason WINTER_END = new SubSeason(-0.35, 0.2, 1.25, 0.75, WINTER_END_WEATHER_EVENT_CONTROLLER, new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));


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
        private final SeasonClient client;
        private final Set<String> entityBreedingBlacklist;


        //These are not to be serialized by GSON.
        private transient SeasonData.SeasonVal parentSeason;
        private transient String subSeason;
        private transient IdentityHashMap<Block, Double> cropToMultiplierStorage;
        private transient IdentityHashMap<ResourceLocation, OverrideStorage> biomeToOverrideStorage;
        private transient ObjectOpenHashSet<EntityType<?>> entityTypeBreedingBlacklist;

        public SubSeason(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, HashMap<String, Double> weatherEventController, SeasonClient client) {
            this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, client, new ObjectOpenHashSet<>());
        }

        public SubSeason(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, HashMap<String, Double> weatherEventController, SeasonClient client, Set<String> entityBreedingBlacklist) {
            this.tempModifier = tempModifier;
            this.humidityModifier = humidityModifier;
            this.weatherEventChanceMultiplier = weatherEventChanceMultiplier;
            this.cropGrowthChanceMultiplier = cropGrowthChanceMultiplier;
            this.weatherEventController = weatherEventController;
            this.client = client;
            this.entityBreedingBlacklist = entityBreedingBlacklist;
        }

        public void processInfo() {
            entityTypeBreedingBlacklist = new ObjectOpenHashSet<>(entityBreedingBlacklist.stream().map(ResourceLocation::new).filter((resourceLocation) -> (BetterWeatherUtil.filterRegistryID(resourceLocation, Registry.ENTITY_TYPE, "Entity"))).map(Registry.ENTITY_TYPE::getOptional).map(Optional::get).collect(Collectors.toSet()));
        }

        public SeasonData.SubSeasonVal getSubSeasonVal() {
            return SeasonData.SubSeasonVal.valueOf(subSeason);
        }

        public void setSubSeasonVal(SeasonData.SubSeasonVal val) {
            subSeason = val.toString();
        }

        public SeasonData.SeasonVal getParentSeason() {
            return parentSeason;
        }

        public void setParentSeason(SeasonData.SeasonVal parentSeason) {
            this.parentSeason = parentSeason;
        }

        public IdentityHashMap<Block, Double> getCropToMultiplierStorage() {
            if (cropToMultiplierStorage == null)
                cropToMultiplierStorage = new IdentityHashMap<>();
            return cropToMultiplierStorage;
        }


        public IdentityHashMap<ResourceLocation, OverrideStorage> getBiomeToOverrideStorage() {
            if (biomeToOverrideStorage == null)
                biomeToOverrideStorage = new IdentityHashMap<>();
            return biomeToOverrideStorage;
        }

        public void setCropToMultiplierStorage(IdentityHashMap<Block, Double> cropToMultiplierStorage) {
            this.cropToMultiplierStorage = cropToMultiplierStorage;
        }

        public void setBiomeToOverrideStorage(IdentityHashMap<ResourceLocation, OverrideStorage> biomeToOverrideStorage) {
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

        public SeasonClient getClient() {
            return client;
        }

        public int getTargetFoliageColor(ResourceLocation biome, boolean useSeasonDefault) {
            int defaultValue = client.parsedFoliageHexColor;
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
            int defaultValue = client.parsedGrassHexColor;
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
            int defaultValue = client.parsedSkyHexColor;
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
            int defaultValue = client.parsedFogHexColor;
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
            return entityTypeBreedingBlacklist;
        }

        public static class SeasonClient {
            private final String targetFoliageHexColor;
            private final double foliageColorBlendStrength;
            private final String targetGrassHexColor;
            private final double grassColorBlendStrength;
            private final String targetSkyHexColor;
            private final double skyColorBlendStrength;
            private final String targetFogHexColor;
            private final double fogColorBlendStrength;
            private transient int parsedFoliageHexColor;
            private transient int parsedGrassHexColor;
            private transient int parsedSkyHexColor;
            private transient int parsedFogHexColor;


            public SeasonClient() {
                this("", 0, "", 0);
            }

            public SeasonClient(String targetFoliageHexColor, double foliageColorBlendStrength, String targetGrassColor, double grassColorBlendStrength) {
                this(targetFoliageHexColor, foliageColorBlendStrength, targetGrassColor, grassColorBlendStrength, targetGrassColor, 0, targetGrassColor, 0);
            }

            public SeasonClient(String targetFoliageHexColor, double foliageColorBlendStrength, String targetGrassColor, double grassColorBlendStrength, String targetSkyHexColor, double skyColorBlendStrength, String targetFogHexColor, double fogColorBlendStrength) {
                this.targetFoliageHexColor = targetFoliageHexColor;
                this.foliageColorBlendStrength = foliageColorBlendStrength;
                this.targetGrassHexColor = targetGrassColor;
                this.grassColorBlendStrength = grassColorBlendStrength;
                this.targetSkyHexColor = targetSkyHexColor;
                this.targetFogHexColor = targetFogHexColor;
                this.fogColorBlendStrength = fogColorBlendStrength;
                this.skyColorBlendStrength = skyColorBlendStrength;
            }

            public void parseHexColors() {
                parsedFoliageHexColor = BetterWeatherUtil.parseHexColor(targetFoliageHexColor);
                parsedGrassHexColor = BetterWeatherUtil.parseHexColor(targetGrassHexColor);
                parsedSkyHexColor = BetterWeatherUtil.parseHexColor(targetSkyHexColor);
                parsedFogHexColor = BetterWeatherUtil.parseHexColor(targetFogHexColor);
            }

            public static int stopSpamIDXFoliage;
            public static int stopSpamIDXGrass;
            public static int stopSpamIDXSky;
            public static int stopSpamIDXFog;
        }
    }
}
