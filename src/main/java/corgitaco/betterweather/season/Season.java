package corgitaco.betterweather.season;

import com.google.common.collect.Sets;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.util.storage.OverrideStorage;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
        map.put(BWSeasonSystem.SeasonVal.SPRING.toString(), SPRING);
        map.put(BWSeasonSystem.SeasonVal.SUMMER.toString(), SUMMER);
        map.put(BWSeasonSystem.SeasonVal.AUTUMN.toString(), AUTUMN);
        map.put(BWSeasonSystem.SeasonVal.WINTER.toString(), WINTER);
    });

    public static Map<String, SubSeason> SUB_SEASON_MAP = Util.make((new TreeMap<>()), (map) -> {
        map.put(BWSeasonSystem.SubSeasonVal.SPRING_START.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.SPRING.toString()).getStart());
        map.put(BWSeasonSystem.SubSeasonVal.SPRING_MID.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.SPRING.toString()).getMid());
        map.put(BWSeasonSystem.SubSeasonVal.SPRING_END.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.SPRING.toString()).getEnd());
        map.put(BWSeasonSystem.SubSeasonVal.SUMMER_START.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.SUMMER.toString()).getStart());
        map.put(BWSeasonSystem.SubSeasonVal.SUMMER_MID.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.SUMMER.toString()).getMid());
        map.put(BWSeasonSystem.SubSeasonVal.SUMMER_END.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.SUMMER.toString()).getEnd());
        map.put(BWSeasonSystem.SubSeasonVal.AUTUMN_START.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.AUTUMN.toString()).getStart());
        map.put(BWSeasonSystem.SubSeasonVal.AUTUMN_MID.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.AUTUMN.toString()).getMid());
        map.put(BWSeasonSystem.SubSeasonVal.AUTUMN_END.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.AUTUMN.toString()).getEnd());
        map.put(BWSeasonSystem.SubSeasonVal.WINTER_START.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.WINTER.toString()).getStart());
        map.put(BWSeasonSystem.SubSeasonVal.WINTER_MID.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.WINTER.toString()).getMid());
        map.put(BWSeasonSystem.SubSeasonVal.WINTER_END.toString(), SEASON_MAP.get(BWSeasonSystem.SeasonVal.WINTER.toString()).getEnd());
    });


    public static Season getSeasonFromEnum(BWSeasonSystem.SeasonVal season) {
        return SEASON_MAP.get(season.toString());
    }

    public static SubSeason getSubSeasonFromEnum(BWSeasonSystem.SubSeasonVal season) {
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

    public boolean containsSubSeason(BWSeasonSystem.SubSeasonVal subSeason) {
        return subSeason == start.getSubSeasonVal() || subSeason == mid.getSubSeasonVal() || subSeason == end.getSubSeasonVal();
    }

    public static class SubSeason {

        public static final SubSeason SPRING_START = new SubSeason(-0.15, 0.5, 1.5, 1.3, new WeatherEventController(0.1, 0.25), new SeasonClient(Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5, Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5));
        public static final SubSeason SPRING_MID = new SubSeason(0.1, 0.5, 2.0, 2.0, new WeatherEventController(0.05, 0.25), new SeasonClient(Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5));
        public static final SubSeason SPRING_END = new SubSeason(0.25, 0.4, 1.5, 1.7, new WeatherEventController(0, 0.25), new SeasonClient(Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5));

        public static final SubSeason SUMMER_START = new SubSeason(0.35, -0.1, 0.75, 1.15, new WeatherEventController(0, 0.25), new SeasonClient());
        public static final SubSeason SUMMER_MID = new SubSeason(0.5, -0.3, 0.2, 1.0, new WeatherEventController(0, 0.25), new SeasonClient());
        public static final SubSeason SUMMER_END = new SubSeason(0.15, -0.1, 0.5, 1.0, new WeatherEventController(0, 0.25), new SeasonClient());

        public static final SubSeason AUTUMN_START = new SubSeason(-0.1, 0, 0.7, 0.8, new WeatherEventController(0, 0.25), new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
        public static final SubSeason AUTUMN_MID = new SubSeason(-0.2, 0, 0.7, 0.75, new WeatherEventController(0.05, 0.25), new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
        public static final SubSeason AUTUMN_END = new SubSeason(-0.3, 0.1, 0.75, 0.65, new WeatherEventController(0.1, 0.25), new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));

        public static final SubSeason WINTER_START = new SubSeason(-0.4, 0.2, 1.0, 0.6, new WeatherEventController(0.3, 0.25), new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
        public static final SubSeason WINTER_MID = new SubSeason(-0.5, 0.2, 1.0, 0.5, new WeatherEventController(0.5, 0.25), new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
        public static final SubSeason WINTER_END = new SubSeason(-0.35, 0.2, 1.25, 0.75, new WeatherEventController(0.3, 0.25), new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));


        public static final IdentityHashMap<Object, OverrideStorage> WINTER_OVERRIDE = Util.make((new IdentityHashMap<>()), (map) -> {
            OverrideStorage overrideStorage = new OverrideStorage();
            overrideStorage.getClientStorage().setTargetFoliageHexColor("#964B00").setTargetGrassHexColor("#964B00"); //Target brown instead of red.
            map.put(Biome.Category.SWAMP, overrideStorage);
        });

        private final double tempModifier;
        private final double humidityModifier;
        private final double weatherEventChanceMultiplier;
        private final double cropGrowthChanceMultiplier; //Final Fallback
        private final WeatherEventController weatherEventController;
        private final SeasonClient client;
        private final Set<String> entityBreedingBlacklist;


        //These are not to be serialized by GSON.
        private transient BWSeasonSystem.SeasonVal parentSeason;
        private transient String subSeason;
        private transient IdentityHashMap<Block, Double> cropToMultiplierStorage;
        private transient IdentityHashMap<ResourceLocation, OverrideStorage> biomeToOverrideStorage;
        private transient ObjectOpenHashSet<EntityType<?>> entityTypeBreedingBlacklist;

        public SubSeason(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, WeatherEventController weatherEventController, SeasonClient client) {
            this(tempModifier, humidityModifier, weatherEventChanceMultiplier, cropGrowthChanceMultiplier, weatherEventController, client, new ObjectOpenHashSet<>());
        }

        public SubSeason(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, double cropGrowthChanceMultiplier, WeatherEventController weatherEventController, SeasonClient client, Set<String> entityBreedingBlacklist) {
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

        public BWSeasonSystem.SubSeasonVal getSubSeasonVal() {
            return BWSeasonSystem.SubSeasonVal.valueOf(subSeason);
        }

        public void setSubSeasonVal(BWSeasonSystem.SubSeasonVal val) {
            subSeason = val.toString();
        }

        public BWSeasonSystem.SeasonVal getParentSeason() {
            return parentSeason;
        }

        public void setParentSeason(BWSeasonSystem.SeasonVal parentSeason) {
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

        public WeatherEventController getWeatherEventController() {
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

            private void printDebugWarning(String message, Object... args) {
                Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage((new StringTextComponent("")).append((new TranslationTextComponent("debug.prefix")).mergeStyle(TextFormatting.RED, TextFormatting.BOLD)).appendString(" ").append(new TranslationTextComponent(message, args)));
            }

            public static int stopSpamIDXFoliage;
            public static int stopSpamIDXGrass;
            public static int stopSpamIDXSky;
            public static int stopSpamIDXFog;
        }
    }


    public static class WeatherEventController {
        private final double blizzardChance;
        private final double acidRainChance;

        public WeatherEventController(double blizzardChance, double acidRainChance) {
            this.blizzardChance = blizzardChance;
            this.acidRainChance = acidRainChance;
        }

        public double getBlizzardChance() {
            return blizzardChance;
        }

        public double getAcidRainChance() {
            return acidRainChance;
        }
    }
}
