package corgitaco.betterweather;

import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.config.json.SeasonConfig;
import corgitaco.betterweather.config.json.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.helper.ViewFrustumGetter;
import corgitaco.betterweather.helper.WeatherViewFrustum;
import corgitaco.betterweather.season.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.nio.file.Path;
import java.util.IdentityHashMap;

@OnlyIn(Dist.CLIENT)
public class BetterWeatherClientUtil {
    private static int configReloadIdx = 0;


    @OnlyIn(Dist.CLIENT)
    public static void configReloadKeybind(int key) {
        Minecraft minecraft = Minecraft.getInstance();

        if (InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), 293) && key == 82) {
            if (configReloadIdx == 0) {
                BetterWeather.loadClientConfigs();
                minecraft.worldRenderer.loadRenderers();
                Season.SubSeason.SeasonClient.stopSpamIDXFoliage = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXGrass = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXSky = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXFog = 0;
                printDebugMessage("bw.debug.reloadconfig.message");
                configReloadIdx = 1;
            }
        } else
            configReloadIdx = 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void printDebugWarning(String message, Object... args) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage((new StringTextComponent("")).append((new TranslationTextComponent("debug.prefix")).mergeStyle(TextFormatting.RED, TextFormatting.BOLD)).appendString(" ").append(new TranslationTextComponent(message, args)));
    }

    @OnlyIn(Dist.CLIENT)
    public static void printDebugMessage(String message, Object... args) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage((new StringTextComponent("")).append((new TranslationTextComponent("debug.prefix")).mergeStyle(TextFormatting.YELLOW, TextFormatting.BOLD)).appendString(" ").append(new TranslationTextComponent(message, args)));
    }

    @OnlyIn(Dist.CLIENT)
    public static void refreshViewFrustum(Minecraft minecraft, int renderDistance) {
        if (!BetterWeather.usingOptifine)
            ((WeatherViewFrustum) ((ViewFrustumGetter) minecraft.worldRenderer).getViewFrustum()).forceRenderDistance(renderDistance, minecraft.player.getPosX(), minecraft.player.getPosY(), minecraft.player.getPosZ());
    }

    public static void loadSeasonConfigsClient() {
        if (BetterWeather.useSeasons) {
            Path seasonConfig = BetterWeather.CONFIG_PATH.resolve(BetterWeather.MOD_ID + "-seasons.json");
            try {
                SeasonConfig.handleBWSeasonsConfig(seasonConfig);
            } catch (Exception e) {
                CrashReport crashReport = CrashReport.makeCrashReport(e, "Reading Season Config");

                BetterWeatherClientUtil.printDebugWarning("bw.reload.season.config.fail", seasonConfig.getFileName(), crashReport.getCrashCause().toString());

                BetterWeather.LOGGER.error("\"" + seasonConfig.getFileName() + "\" failed to load because of the following error(s): " + crashReport.getCompleteReport() + "\n Using shipped default..." );
                Season.SUB_SEASON_MAP = Season.FALLBACK_MAP;
            }


            Season.SUB_SEASON_MAP.forEach((subSeasonName, subSeason) -> {
                Path overrideFilePath = BetterWeather.CONFIG_PATH.resolve("overrides").resolve(subSeasonName + "-override.json");
                if (subSeason.getParentSeason() == SeasonData.SeasonVal.WINTER) {
                    try {
                        BiomeOverrideJsonHandler.handleOverrideJsonConfigs(overrideFilePath, Season.SubSeason.WINTER_OVERRIDE, subSeason);
                    } catch (Exception e) {
                        CrashReport crashReport = CrashReport.makeCrashReport(e, "Reading Subseason Override Config");
                        BetterWeatherClientUtil.printDebugWarning("bw.reload.seasonoverride.config.fail", overrideFilePath.getFileName(), crashReport.getCrashCause().toString());

                        BetterWeather.LOGGER.error("Override Config: \"" + overrideFilePath.getFileName() + "\" failed to load because of the following error(s): " + crashReport.getCompleteReport() + "\n Doing nothing..." );

                        subSeason.setCropToMultiplierStorage(new IdentityHashMap<>());
                        subSeason.setBiomeToOverrideStorage(new IdentityHashMap<>());
                    }
                }
                else {
                    try {
                        BiomeOverrideJsonHandler.handleOverrideJsonConfigs(overrideFilePath, new IdentityHashMap<>(), subSeason);
                    } catch (Exception e) {
                        BetterWeatherClientUtil.printDebugWarning("bw.reloadconfig.fail", overrideFilePath.getFileName(), e.toString());

                        BetterWeather.LOGGER.error("Override Config: \"" + overrideFilePath.getFileName() + "\" failed to load! Doing nothing..." );

                        subSeason.setCropToMultiplierStorage(new IdentityHashMap<>());
                        subSeason.setBiomeToOverrideStorage(new IdentityHashMap<>());
                    }
                }
            });
        }
    }
}
