package corgitaco.betterweather.season.client;

import corgitaco.betterweather.helper.BetterWeatherWorldData;
import corgitaco.betterweather.season.SeasonContext;
import corgitaco.betterweather.season.SubSeasonSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class BiomeColorCalculator {

    public static Color modifyBiomeColor(ColorType colorType, Color originalColorValue, Biome biome) {
        Color fallbackColor = originalColorValue;  //modifyColorForWeatherEvent(colorType, originalColorValue, originalColorValue, null);
        Minecraft mc = Minecraft.getInstance();

        ClientWorld world = mc.world;

        if (world == null)
            return fallbackColor;

        SeasonContext seasonContext = ((BetterWeatherWorldData) mc.world).getSeasonContext();
        if (seasonContext == null)
            return fallbackColor;

        SubSeasonSettings subSeasonSettings = seasonContext.getCurrentSubSeasonSettings();

        int red = originalColorValue.getRed();
        int green = originalColorValue.getGreen();
        int blue = originalColorValue.getBlue();

        Color target;
        double blendStrength;
        DynamicRegistries dynamicRegistries = world.func_241828_r();
        ResourceLocation biomeKey = dynamicRegistries.getRegistry(Registry.BIOME_KEY).getKey(biome);
        if (biomeKey == null)
            return fallbackColor;

        switch (colorType) {
            case GRASS:
                int targetGrassColor = subSeasonSettings.getTargetGrassColor(biomeKey, false);

                if (targetGrassColor == Integer.MAX_VALUE)
                    return fallbackColor;

                target = new Color(targetGrassColor);
                blendStrength = subSeasonSettings.getGrassColorBlendStrength(biomeKey, false);
                break;
            case FOLIAGE:
                int targetFoliageColor = subSeasonSettings.getTargetFoliageColor(biomeKey, false);

                if (targetFoliageColor == Integer.MAX_VALUE)
                    return fallbackColor;

                target = new Color(targetFoliageColor);
                blendStrength = subSeasonSettings.getFoliageColorBlendStrength(biomeKey, false);
                break;
            case FOG:
                int targetFogColor = subSeasonSettings.getTargetFogColor(biomeKey, false);

                if (targetFogColor == Integer.MAX_VALUE)
                    return fallbackColor;

                target = new Color(targetFogColor);
                blendStrength = subSeasonSettings.getFogColorBlendStrength(biomeKey, false);
                break;
            default:
                int targetSkyColor = subSeasonSettings.getTargetSkyColor(biomeKey, false);

                if (targetSkyColor == Integer.MAX_VALUE)
                    return fallbackColor;

                target = new Color(targetSkyColor);
                blendStrength = subSeasonSettings.getSkyColorBlendStrength(biomeKey, false);
                break;
        }

        red = modifiedColorValue(red, target.getRed(), blendStrength);
        green = modifiedColorValue(green, target.getGreen(), blendStrength);
        blue = modifiedColorValue(blue, target.getBlue(), blendStrength);

        int clampedRed = MathHelper.clamp(red, 0, 255);
        int clampedGreen = MathHelper.clamp(green, 0, 255);
        int clampedBlue = MathHelper.clamp(blue, 0, 255);

        Color modifiedColor = new Color(clampedRed, clampedGreen, clampedBlue);
        return modifiedColor;  // modifyColorForWeatherEvent(colorType, originalColorValue, modifiedColor, target);
    }


//    public static Color modifyColorForWeatherEvent(ColorType type, Color originalBiomeColor, Color returnColor, @Nullable Color seasonTargetColor) {
//        Minecraft instance = Minecraft.getInstance();
//        float partialTicks = instance.isGamePaused() ? instance.renderPartialTicksPaused : instance.timer.renderPartialTicks;
//
//        switch (type) {
//            case GRASS:
//                return WeatherData.currentWeatherEvent.modifyGrassColor(originalBiomeColor, returnColor, seasonTargetColor);
//            case FOLIAGE:
//                return WeatherData.currentWeatherEvent.modifyFoliageColor(originalBiomeColor, returnColor, seasonTargetColor);
//            case SKY:
//                return WeatherData.currentWeatherEvent.modifySkyColor(originalBiomeColor, returnColor, seasonTargetColor, instance.world.getRainStrength(partialTicks));
//            case FOG:
//                return WeatherData.currentWeatherEvent.modifyFogColor(originalBiomeColor, returnColor, seasonTargetColor, instance.world.getRainStrength(partialTicks));
//        }
//        return originalBiomeColor;
//    }

    private static int modifiedColorValue(int original, int target, double blendStrength) {
        return (int) MathHelper.lerp(blendStrength, original, target);
    }

    public enum ColorType {
        FOLIAGE,
        GRASS,
        SKY,
        FOG
    }
}
