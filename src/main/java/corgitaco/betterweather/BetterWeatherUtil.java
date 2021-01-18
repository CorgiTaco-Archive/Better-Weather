package corgitaco.betterweather;

import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.config.json.SeasonConfig;
import corgitaco.betterweather.config.json.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.season.Season;
import net.minecraft.command.CommandSource;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.awt.*;
import java.nio.file.Path;
import java.util.IdentityHashMap;

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
            BetterWeather.LOGGER.error("\"" + id.toString() + "\" was not a registryID in the " + registryTypeName + "! Skipping entry...");
            return false;
        }
    }

    public static int transformRainOrThunderTimeToCurrentSeason(int rainOrThunderTime, Season.SubSeason previous, Season.SubSeason current) {
        double previousMultiplier = previous.getWeatherEventChanceMultiplier();
        double currentMultiplier = current.getWeatherEventChanceMultiplier();
        double normalTime = rainOrThunderTime * previousMultiplier;

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


    public static boolean isOverworld(RegistryKey<World> worldKey) {
        return worldKey == World.OVERWORLD;
    }

    public static void loadSeasonConfigsServer(@Nullable CommandSource source) {
        if (BetterWeather.useSeasons) {
            Path seasonConfig = BetterWeather.CONFIG_PATH.resolve(BetterWeather.MOD_ID + "-seasons.json");
            try {
                SeasonConfig.handleBWSeasonsConfig(seasonConfig);
            } catch (Exception e) {
                CrashReport crashReport = CrashReport.makeCrashReport(e, "Reading Season Config");
                if (source == null) {
                    BetterWeatherClientUtil.printDebugWarning("bw.reload.season.config.fail", seasonConfig.getFileName(), crashReport.getCrashCause().toString());
                }

                if (source != null)
                    source.sendFeedback(new TranslationTextComponent("bw.reload.season.config.fail", seasonConfig.getFileName(), crashReport.getCrashCause().toString()), true);

                BetterWeather.LOGGER.error("\"" + seasonConfig.getFileName() + "\" failed to load because of the following error(s): " + crashReport.getCompleteReport() + "\n Using shipped default...");
                Season.SUB_SEASON_MAP = Season.FALLBACK_MAP;
            }


            Season.SUB_SEASON_MAP.forEach((subSeasonName, subSeason) -> {
                Path overrideFilePath = BetterWeather.CONFIG_PATH.resolve("overrides").resolve(subSeasonName + "-override.json");
                if (subSeason.getParentSeason() == SeasonData.SeasonVal.WINTER) {
                    try {
                        BiomeOverrideJsonHandler.handleOverrideJsonConfigs(overrideFilePath, Season.SubSeason.WINTER_OVERRIDE, subSeason);
                    } catch (Exception e) {
                        CrashReport crashReport = CrashReport.makeCrashReport(e, "Reading Subseason Override Config");
                        if (source != null)
                            source.sendFeedback(new TranslationTextComponent("bw.reload.seasonoverride.config.fail", overrideFilePath.getFileName(), crashReport.getCrashCause().toString()), true);

                        BetterWeather.LOGGER.error("Override Config: \"" + overrideFilePath.getFileName() + "\" failed to load because of the following error(s): " + crashReport.getCompleteReport() + "\n Doing nothing...");

                        subSeason.setCropToMultiplierStorage(new IdentityHashMap<>());
                        subSeason.setBiomeToOverrideStorage(new IdentityHashMap<>());
                    }
                } else {
                    try {
                        BiomeOverrideJsonHandler.handleOverrideJsonConfigs(overrideFilePath, new IdentityHashMap<>(), subSeason);
                    } catch (Exception e) {
                        if (source == null)
                            BetterWeatherClientUtil.printDebugWarning("bw.reloadconfig.fail", overrideFilePath.getFileName(), e.toString());

                        if (source != null)
                            source.sendFeedback(new TranslationTextComponent("bw.reloadconfig.fail", overrideFilePath.getFileName(), e.toString()), true);

                        BetterWeather.LOGGER.error("Override Config: \"" + overrideFilePath.getFileName() + "\" failed to load! Doing nothing...");

                        subSeason.setCropToMultiplierStorage(new IdentityHashMap<>());
                        subSeason.setBiomeToOverrideStorage(new IdentityHashMap<>());
                    }
                }
            });
        }
    }
}