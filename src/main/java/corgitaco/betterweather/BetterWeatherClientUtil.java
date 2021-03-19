package corgitaco.betterweather;

import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.config.season.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.helper.ViewFrustumGetter;
import corgitaco.betterweather.helper.WeatherViewFrustum;
import corgitaco.betterweather.season.SubSeasonSettings;
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
}
