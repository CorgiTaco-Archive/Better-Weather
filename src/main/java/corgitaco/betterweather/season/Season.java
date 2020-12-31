package corgitaco.betterweather.season;

import com.google.common.collect.Sets;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.seasonoverrides.SeasonOverrides;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.util.*;

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


    public static SeasonOverrides seasonOverrides;


    public static Season getSeasonFromEnum(BWSeasonSystem.SeasonVal season) {
        return SEASON_MAP.get(season.toString());
    }

    public static SubSeason getSubSeasonFromEnum(BWSeasonSystem.SubSeasonVal season) {
        return SUB_SEASON_MAP.get(season.toString());
    }


    private final SubSeason start;
    private final SubSeason mid;
    private final SubSeason end;

    private final Set<SubSeason> subSeasons;

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

        public static final SubSeason SPRING_START = new SubSeason(-0.15, 0.5, 1.5, "override", new WeatherEventController(0.1, 0.25), new SeasonClient(Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5, Integer.toHexString(new Color(51, 97, 50).getRGB()), 0.5));
        public static final SubSeason SPRING_MID = new SubSeason(0.1, 0.5, 2.0, "override", new WeatherEventController(0.05, 0.25), new SeasonClient(Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(41, 87, 2).getRGB()), 0.5));
        public static final SubSeason SPRING_END = new SubSeason(0.25, 0.4, 1.5, "override", new WeatherEventController(0, 0.25), new SeasonClient(Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5, Integer.toHexString(new Color(20, 87, 2).getRGB()), 0.5));

        public static final SubSeason SUMMER_START = new SubSeason(0.35, -0.1, 0.75, "1.0", new WeatherEventController(0, 0.25), new SeasonClient());
        public static final SubSeason SUMMER_MID = new SubSeason(0.5, -0.3, 0.2, "1.0", new WeatherEventController(0, 0.25), new SeasonClient());
        public static final SubSeason SUMMER_END = new SubSeason(0.15, -0.1, 0.5, "1.0", new WeatherEventController(0, 0.25), new SeasonClient());

        public static final SubSeason AUTUMN_START = new SubSeason(-0.1, 0, 0.7, "override", new WeatherEventController(0, 0.25), new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
        public static final SubSeason AUTUMN_MID = new SubSeason(-0.2, 0, 0.7, "override", new WeatherEventController(0.05, 0.25), new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));
        public static final SubSeason AUTUMN_END = new SubSeason(-0.3, 0.1, 0.75, "override", new WeatherEventController(0.1, 0.25), new SeasonClient(Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5, Integer.toHexString(new Color(155, 103, 60).getRGB()), 0.5));

        public static final SubSeason WINTER_START = new SubSeason(-0.4, 0.2, 1.0, "override", new WeatherEventController(0.3, 0.25), new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
        public static final SubSeason WINTER_MID = new SubSeason(-0.5, 0.2, 1.0, "override", new WeatherEventController(0.5, 0.25), new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));
        public static final SubSeason WINTER_END = new SubSeason(-0.35, 0.2, 1.25, "override", new WeatherEventController(0.3, 0.25), new SeasonClient(Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5, Integer.toHexString(new Color(165, 42, 42).getRGB()), 0.5));


        private final double tempModifier;
        private final double humidityModifier;
        private final double weatherEventChanceMultiplier;
        private final String cropGrowthChanceMultiplier;
        private final WeatherEventController weatherEventController;
        private final SeasonClient client;

        //These are not to be serialized by GSON.
        private transient BWSeasonSystem.SeasonVal parentSeason;
        private transient String subSeason;
        private transient Map<String, Double> cropToMultiplierMap;
        private transient IdentityHashMap<Block, Double> cropToMultiplierIdentityHashMap;
        private transient double cropGrowthMultiplier;

        public SubSeason(double tempModifier, double humidityModifier, double weatherEventChanceMultiplier, String cropGrowthChanceMultiplier, WeatherEventController weatherEventController, SeasonClient client) {
            this.tempModifier = tempModifier;
            this.humidityModifier = humidityModifier;
            this.weatherEventChanceMultiplier = weatherEventChanceMultiplier;
            this.cropGrowthChanceMultiplier = cropGrowthChanceMultiplier;
            this.weatherEventController = weatherEventController;
            this.client = client;
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

        public boolean testCropGrowthMultiplier() {
            try{
                Double.parseDouble(cropGrowthChanceMultiplier);
                return false;
            } catch (NumberFormatException exception) {
                BetterWeather.LOGGER.info("Using crop growth override file for: \"" + getSubSeasonVal().toString() + "\".");
                return true;
            }
        }

        public Map<String, Double> getCropToMultiplierMap() {
            if (cropToMultiplierMap == null)
                cropToMultiplierMap = new HashMap<>();
            return cropToMultiplierMap;
        }

        public IdentityHashMap<Block, Double> getCropToMultiplierIdentityHashMap() {
            if (cropToMultiplierIdentityHashMap == null)
                cropToMultiplierIdentityHashMap = new IdentityHashMap<>();
            return cropToMultiplierIdentityHashMap;
        }

        public double getTempModifier() {
            return tempModifier;
        }

        public double getHumidityModifier() {
            return humidityModifier;
        }

        public double getWeatherEventChanceMultiplier() {
            return weatherEventChanceMultiplier;
        }

        public double getCropGrowthChanceMultiplier(String blockName, Block block, boolean parseDouble) {
            double value = 1.0;

            if (parseDouble) {
                try {
                    if (cropGrowthMultiplier == 0.0)
                        cropGrowthMultiplier = Double.parseDouble(cropGrowthChanceMultiplier);
                    return cropGrowthMultiplier;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Could not read numerical value for cropGrowthMultiplier! You put: " + cropGrowthChanceMultiplier + ".\n This value needs to need be a numerical value or point to a config file in your \"betterweather/overrides\" folder.");
                }
            } else if (cropToMultiplierMap.containsKey(blockName)) {
                value = cropToMultiplierMap.get(blockName);
            }
            return value;
        }

        public WeatherEventController getWeatherEventController() {
            return weatherEventController;
        }

        public SeasonClient getClient() {
            return client;
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

            private void printDebugWarning(String message, Object... args) {
                Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage((new StringTextComponent("")).append((new TranslationTextComponent("debug.prefix")).mergeStyle(TextFormatting.RED, TextFormatting.BOLD)).appendString(" ").append(new TranslationTextComponent(message, args)));
            }

            public static int stopSpamIDXFoliage;
            public static int stopSpamIDXGrass;
            public static int stopSpamIDXSky;
            public static int stopSpamIDXFog;
            static int spamMaxIDX = 0;

            public int getTargetFoliageColor() {
                if (targetFoliageHexColor.isEmpty())
                    return -1;
                else {
                    try {
                        return (int) Long.parseLong(targetFoliageHexColor.replace("#", "").replace("0x", ""), 16);
                    } catch (Exception e) {
                        if (stopSpamIDXFoliage <= spamMaxIDX) {
                            this.printDebugWarning("bw.debug.warn.colorerror", "targetFoliageHexColor", targetFoliageHexColor);
                            BetterWeather.LOGGER.warn("targetFoliageHexColor was not a hex color value, you put: \"" + targetFoliageHexColor + "\" | Using Defaults...");
                            stopSpamIDXFoliage++;
                        }
                    }
                    return -1;
                }
            }

            public double getFoliageColorBlendStrength() {
                return foliageColorBlendStrength;
            }

            public int getTargetGrassColor() {
                if (targetGrassHexColor.isEmpty())
                    return -1;
                else {
                    try {
                        return (int) Long.parseLong(targetGrassHexColor.replace("#", "").replace("0x", ""), 16);
                    } catch (Exception e) {
                        if (stopSpamIDXGrass <= spamMaxIDX) {
                            this.printDebugWarning("bw.debug.warn.colorerror", "targetGrassHexColor", targetGrassHexColor);
                            BetterWeather.LOGGER.warn("targetGrassHexColor was not a hex color value, you put: \"" + targetGrassHexColor + "\" | Using Defaults...");
                            stopSpamIDXGrass++;
                        }
                    }
                    return -1;
                }
            }

            public double getGrassColorBlendStrength() {
                return grassColorBlendStrength;
            }

            public int getTargetSkyColor() {
                if (targetSkyHexColor.isEmpty())
                    return -1;
                else {
                    try {
                        return (int) Long.parseLong(targetSkyHexColor.replace("#", "").replace("0x", ""), 16);
                    } catch (Exception e) {
                        if (stopSpamIDXSky <= spamMaxIDX) {
                            this.printDebugWarning("bw.debug.warn.colorerror", "targetSkyHexColor", targetSkyHexColor);
                            BetterWeather.LOGGER.warn("targetSkyHexColor was not a hex color value, you put: \"" + targetSkyHexColor + "\" | Using Defaults...");
                            stopSpamIDXSky++;
                        }
                    }
                    return -1;
                }
            }

            public double getSkyColorBlendStrength() {
                return skyColorBlendStrength;
            }

            public int getTargetFogColor() {
                if (targetFogHexColor.isEmpty())
                    return -1;
                else {
                    try {
                        return (int) Long.parseLong(targetGrassHexColor.replace("#", "").replace("0x", ""), 16);
                    } catch (Exception e) {
                        if (stopSpamIDXFog <= spamMaxIDX) {
                            this.printDebugWarning("bw.debug.warn.colorerror", "targetFogHexColor", targetFogHexColor);
                            BetterWeather.LOGGER.warn("targetFogHexColor was not a hex color value, you put: \"" + targetFogHexColor + "\" | Using Defaults...");
                            stopSpamIDXFog++;
                        }
                    }
                    return -1;
                }
            }

            public double getFogColorBlendStrength() {
                return fogColorBlendStrength;
            }
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
